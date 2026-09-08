package br.com.miniautorizador.controller.cartao;

import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.service.cartao.CartaoService;
import br.com.miniautorizador.model.cartao.request.CreateCartaoRequest;
import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private String body(String number, String password) {
        return """
            {"numeroCartao":"%s","senha":"%s"}
            """.formatted(number, password);
    }

    private String cardNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
