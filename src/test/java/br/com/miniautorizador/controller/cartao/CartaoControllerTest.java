package br.com.miniautorizador.controller.cartao;

import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.service.cartao.CartaoService;
import br.com.miniautorizador.model.cartao.request.CreateCartaoRequest;
import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.exceptions.cartao.CartaoNotFoundException;
import br.com.miniautorizador.api.ApiExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;
import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartaoControllerTest {

    private final CartaoService service = mock(CartaoService.class);
    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new CartaoController(service))
            .setControllerAdvice(new ApiExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @AfterEach
    void closeValidator() {
        validator.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"500.00", "495.15", "0.00"})
    void returnsOnlyNumericBalance(String balance) throws Exception {
        when(service.findByCardNumber("0123"))
            .thenReturn(Cartao.restore("0123", "1234", new BigDecimal(balance)));

        mvc.perform(get("/cartoes/0123"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(balance));

        verify(service).findByCardNumber("0123");
        verifyNoMoreInteractions(service);
    }

    @Test
    void returns404WithoutBodyForUnknownCard() throws Exception {
        when(service.findByCardNumber("0123")).thenThrow(new CartaoNotFoundException());

        mvc.perform(get("/cartoes/0123"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));

        verify(service).findByCardNumber("0123");
        verifyNoMoreInteractions(service);
    }

    @Test
    void createsCardWithExactResponse() throws Exception {
        var number = cardNumber();
        var body = body(number, "0123");
        var request = new CreateCartaoRequest(number, "0123");
        when(service.create(request)).thenReturn(Cartao.create(number, "stored-password"));

        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(body, JsonCompareMode.STRICT));

        verify(service).create(request);
    }

    @Test
    void duplicateReturnsSubmittedPassword() throws Exception {
        var number = cardNumber();
        when(service.create(new CreateCartaoRequest(number, "9876")))
            .thenThrow(new DuplicateCartaoException(number, "9876"));
        var body = body(number, "9876");

        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().is(422))
            .andExpect(content().json(body, JsonCompareMode.STRICT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{", "{\"numeroCartao\":\"123\"}",
        "{\"numeroCartao\":\"\",\"senha\":\"1234\"}",
        "{\"numeroCartao\":\"123\",\"senha\":\"   \"}"})
    void rejectsMissingFieldsBlankFieldsAndMalformedJson(String body) throws Exception {
        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsValuesBeyondDatabaseLimits() throws Exception {
        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON)
                .content(body("1".repeat(33), "1234")))
            .andExpect(status().isBadRequest());

        var number = cardNumber();
        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON)
                .content(body(number, "1".repeat(101))))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345", "12a4", "１２３４", " 1234", "1234 ", "12.4"})
    void rejectsPasswordOutsideFourAsciiDigits(String password) throws Exception {
        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON)
                .content(body("0123", password)))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0000", "0123", "9876"})
    void acceptsFourDigitsAndReturnsSubmittedPassword(String password) throws Exception {
        var request = new CreateCartaoRequest("0123", password);
        when(service.create(request)).thenReturn(Cartao.create("0123", "hash-simulado"));
        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON)
                .content(body("0123", password)))
            .andExpect(status().isCreated())
            .andExpect(content().json(body("0123", password), JsonCompareMode.STRICT));
        verify(service).create(request);
    }

    @Test
    void explainsInvalidPasswordWithoutExposingSubmittedValue() throws Exception {
        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON)
                .content(body("0123", "12345")))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                {"status":400,"erros":[{"campo":"senha","mensagem":"A senha deve conter exatamente 4 dígitos numéricos."}]}
                """, JsonCompareMode.STRICT));
        verifyNoInteractions(service);
    }

    @Test
    void reportsAllMissingFields() throws Exception {
        mvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {"status":400,"erros":[
                  {"campo":"numeroCartao","mensagem":"O número do cartão é obrigatório."},
                  {"campo":"senha","mensagem":"A senha é obrigatória."}
                ]}
                """, JsonCompareMode.STRICT));
        verifyNoInteractions(service);
    }

    private String body(String number, String password) {
        return """
            {"numeroCartao":"%s","senha":"%s"}
            """.formatted(number, password);
    }

    private String cardNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
