package br.com.miniautorizador.controller.transacao;

import br.com.miniautorizador.api.ApiExceptionHandler;
import br.com.miniautorizador.exceptions.transacao.TransacaoNegadaException;
import br.com.miniautorizador.model.transacao.request.TransacaoRequest;
import br.com.miniautorizador.service.transacao.TransacaoService;
import br.com.miniautorizador.util.enums.MotivoNegacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransacaoControllerTest {
    private final TransacaoService service = mock(TransacaoService.class);
    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new TransacaoController(service))
            .setControllerAdvice(new ApiExceptionHandler()).setValidator(validator).build();
    }

    @AfterEach
    void closeValidator() {
        validator.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "10.0", "10.00", "0.01", "500.00"})
    void returns201WithPlainOk(String amount) throws Exception {
        when(service.authorize(any())).thenReturn("OK");
        mvc.perform(post("/transacoes").contentType(MediaType.APPLICATION_JSON).content(body(amount)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string("OK"));
        verify(service).authorize(new TransacaoRequest("0123", "0123", new BigDecimal(amount)));
        verifyNoMoreInteractions(service);
    }

    @ParameterizedTest
    @EnumSource(MotivoNegacao.class)
    void returns422WithExactDenialCode(MotivoNegacao reason) throws Exception {
        doThrow(new TransacaoNegadaException(reason)).when(service).authorize(any());
        mvc.perform(post("/transacoes").contentType(MediaType.APPLICATION_JSON).content(body("10.00")))
            .andExpect(status().is(422))
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string(reason.name()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.001", "10.001", "10.000", "null", "100000000000000000.00"})
    void rejectsInvalidAmountsBeforeCallingService(String amount) throws Exception {
        mvc.perform(post("/transacoes").contentType(MediaType.APPLICATION_JSON).content(body(amount)))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{",
        "{\"numeroCartao\":\"0123\",\"valor\":10}",
        "{\"senhaCartao\":\"0123\",\"valor\":10}",
        "{\"numeroCartao\":\" \" ,\"senhaCartao\":\"0123\",\"valor\":10}",
        "{\"numeroCartao\":\"0123\",\"senhaCartao\":\" \" ,\"valor\":10}"})
    void rejectsInvalidPayloadBeforeCallingService(String payload) throws Exception {
        mvc.perform(post("/transacoes").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @org.junit.jupiter.api.Test
    void explainsInvalidAmount() throws Exception {
        mvc.perform(post("/transacoes").contentType(MediaType.APPLICATION_JSON).content(body("0")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.erros[0].campo").value("valor"))
            .andExpect(jsonPath("$.erros[0].mensagem").value("O valor deve ser maior ou igual a 0.01."));
        verifyNoInteractions(service);
    }

    private String body(String amount) {
        return """
            {"numeroCartao":"0123","senhaCartao":"0123","valor":%s}
            """.formatted(amount);
    }
}
