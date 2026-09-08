package br.com.miniautorizador.repository.cartao;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.repository.cartao.entity.CartaoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartaoPersistenceAdapterTest {
    @Test
    void translatesAffectedRowsIntoDebitResult() {
        var balance = new BigDecimal("490.00");
        when(repository.debitIfSufficientBalance("0123", balance)).thenReturn(1, 0);
        assertThat(adapter.debitIfSufficientBalance("0123", balance)).isTrue();
        assertThat(adapter.debitIfSufficientBalance("0123", balance)).isFalse();
        verify(repository, times(2)).debitIfSufficientBalance("0123", balance);
        verifyNoMoreInteractions(repository);
    }
    private final JpaCartaoRepository repository = mock(JpaCartaoRepository.class);
    private final CartaoPersistenceAdapter adapter = new CartaoPersistenceAdapter(repository);

    @Test
    void insertsWithoutUsingMerge() {
        var card = Cartao.create("0123", "1234");
        assertThat(adapter.insert(card)).isSameAs(card);
        verify(repository).insert("0123", "1234", new BigDecimal("500.00"));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void mapsExistingCardWithoutResettingBalance() {
        when(repository.findById("0123")).thenReturn(Optional.of(
            new CartaoEntity("0123", "1234", new BigDecimal("495.15"))));
        assertThat(adapter.findByCardNumber("0123")).hasValueSatisfying(card -> {
            assertThat(card.getNumeroCartao()).isEqualTo("0123");
            assertThat(card.getSenha()).isEqualTo("1234");
            assertThat(card.getSaldo()).isEqualTo(new BigDecimal("495.15"));
        });
    }

    @Test
    void returnsEmptyForUnknownCard() {
        when(repository.findById("0123")).thenReturn(Optional.empty());
        assertThat(adapter.findByCardNumber("0123")).isEmpty();
    }

    @Test
    void delegatesExistenceCheck() {
        when(repository.existsById("0123")).thenReturn(true, false);
        assertThat(adapter.existsByCardNumber("0123")).isTrue();
        assertThat(adapter.existsByCardNumber("0123")).isFalse();
    }

    @Test
    void translatesMysqlDuplicateWithSubmittedData() {
        var error = new DataIntegrityViolationException("duplicate",
            new SQLException("duplicate key", "23000", 1062));
        doThrow(error).when(repository).insert(any(), any(), any());
        assertThatThrownBy(() -> adapter.insert(Cartao.create("0123", "9876")))
            .isInstanceOfSatisfying(DuplicateCartaoException.class, exception -> {
                assertThat(exception.getNumeroCartao()).isEqualTo("0123");
                assertThat(exception.getSenha()).isEqualTo("9876");
            });
        verify(repository).insert("0123", "9876", new BigDecimal("500.00"));
        verifyNoMoreInteractions(repository);
    }
}
