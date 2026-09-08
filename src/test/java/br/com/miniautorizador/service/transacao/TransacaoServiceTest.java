package br.com.miniautorizador.service.transacao;

import br.com.miniautorizador.exceptions.transacao.TransacaoNegadaException;
import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.model.transacao.request.TransacaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import br.com.miniautorizador.util.enums.MotivoNegacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransacaoServiceTest {
    private final CartaoRepository repository = mock(CartaoRepository.class);
    private final TransacaoService service = new TransacaoService(repository);

    @ParameterizedTest
    @ValueSource(strings = {"10.00", "500.00", "0.01", "10"})
    void debitsExactAmount(String amount) {
        when(repository.findByCardNumber("0123"))
            .thenReturn(Optional.of(Cartao.create("0123", "0123")));
        when(repository.debitIfSufficientBalance("0123", new BigDecimal(amount))).thenReturn(true);
        assertThat(service.authorize(request("0123", amount))).isEqualTo("OK");
        var order = inOrder(repository);
        order.verify(repository).findByCardNumber("0123");
        order.verify(repository).debitIfSufficientBalance("0123", new BigDecimal(amount));
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
        when(repository.debitIfSufficientBalance("0123", new BigDecimal("500.01"))).thenReturn(false);
        assertThatThrownBy(() -> service.authorize(request("0123", "500.01")))
            .isInstanceOfSatisfying(TransacaoNegadaException.class,
                exception -> assertThat(exception.getMotivo()).isEqualTo(MotivoNegacao.SALDO_INSUFICIENTE));
        verify(repository).findByCardNumber("0123");
        verify(repository).debitIfSufficientBalance("0123", new BigDecimal("500.01"));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deniesWhenDatabaseBalanceChangedSinceRead() {
        when(repository.findByCardNumber("0123"))
            .thenReturn(Optional.of(Cartao.create("0123", "0123")));
        when(repository.debitIfSufficientBalance("0123", new BigDecimal("10.00"))).thenReturn(false);
        assertThatThrownBy(() -> service.authorize(request("0123", "10.00")))
            .isInstanceOfSatisfying(TransacaoNegadaException.class,
                exception -> assertThat(exception.getMotivo()).isEqualTo(MotivoNegacao.SALDO_INSUFICIENTE));
    }

    @Test
    void propagatesWriteFailure() {
        when(repository.findByCardNumber("0123"))
            .thenReturn(Optional.of(Cartao.create("0123", "0123")));
        var error = new IllegalStateException("database unavailable");
        when(repository.debitIfSufficientBalance(any(), any())).thenThrow(error);
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
