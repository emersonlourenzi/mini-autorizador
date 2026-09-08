package br.com.miniautorizador.service.transacao;

import br.com.miniautorizador.exceptions.transacao.TransacaoNegadaException;
import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.model.transacao.request.TransacaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import br.com.miniautorizador.util.enums.MotivoNegacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransacaoServiceTest {
    private final CartaoRepository repository = mock(CartaoRepository.class);
    private final TransacaoService service = new TransacaoService(repository);

    @ParameterizedTest
    @CsvSource({"10.00,490.00", "500.00,0.00", "0.01,499.99", "10,490.00"})
    void debitsExactAmount(String amount, String expectedBalance) {
        when(repository.findByCardNumber("0123"))
            .thenReturn(Optional.of(Cartao.create("0123", "0123")));
        service.authorize(request("0123", amount));
        var order = inOrder(repository);
        order.verify(repository).findByCardNumber("0123");
        order.verify(repository).updateBalance("0123", new BigDecimal(expectedBalance));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rejectsUnknownCardWithoutDebit() {
        when(repository.findByCardNumber("0123")).thenReturn(Optional.empty());
        assertDenied(request("0123", "10.00"), MotivoNegacao.CARTAO_INEXISTENTE);
    }

    @Test
    void rejectsWrongPasswordBeforeCheckingInsufficientBalance() {
        when(repository.findByCardNumber("0123"))
            .thenReturn(Optional.of(Cartao.restore("0123", "0123", BigDecimal.ZERO)));
        assertDenied(request("9999", "10.00"), MotivoNegacao.SENHA_INVALIDA);
    }

    @Test
    void rejectsInsufficientBalanceWithoutDebit() {
        when(repository.findByCardNumber("0123"))
            .thenReturn(Optional.of(Cartao.create("0123", "0123")));
        assertDenied(request("0123", "500.01"), MotivoNegacao.SALDO_INSUFICIENTE);
    }

    @Test
    void propagatesWriteFailure() {
        when(repository.findByCardNumber("0123"))
            .thenReturn(Optional.of(Cartao.create("0123", "0123")));
        var error = new IllegalStateException("database unavailable");
        doThrow(error).when(repository).updateBalance(any(), any());
        assertThatThrownBy(() -> service.authorize(request("0123", "10.00"))).isSameAs(error);
    }

    private void assertDenied(TransacaoRequest request, MotivoNegacao reason) {
        assertThatThrownBy(() -> service.authorize(request))
            .isInstanceOfSatisfying(TransacaoNegadaException.class,
                exception -> assertThat(exception.getMotivo()).isEqualTo(reason));
        verify(repository).findByCardNumber("0123");
        verifyNoMoreInteractions(repository);
    }

    private TransacaoRequest request(String password, String amount) {
        return new TransacaoRequest("0123", password, new BigDecimal(amount));
    }
}
