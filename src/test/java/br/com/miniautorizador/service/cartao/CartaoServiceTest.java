package br.com.miniautorizador.service.cartao;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.exceptions.cartao.CartaoNotFoundException;
import br.com.miniautorizador.model.cartao.Cartao;
import java.util.Optional;
import br.com.miniautorizador.model.cartao.request.CreateCartaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import org.junit.jupiter.api.Test;
import br.com.miniautorizador.service.security.PasswordHasher;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartaoServiceTest {

    @Test
    void returnsExistingCardWithCurrentBalance() {
        var card = Cartao.restore("0123", "1234", new BigDecimal("495.15"));
        when(repository.findByCardNumber("0123")).thenReturn(Optional.of(card));

        assertThat(service.findByCardNumber("0123")).isSameAs(card);
        verify(repository).findByCardNumber("0123");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rejectsBalanceQueryForUnknownCard() {
        when(repository.findByCardNumber("0123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCardNumber("0123"))
            .isInstanceOf(CartaoNotFoundException.class);
        verify(repository).findByCardNumber("0123");
        verifyNoMoreInteractions(repository);
    }
    private final CartaoRepository repository = mock(CartaoRepository.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final CartaoService service = new CartaoService(repository, hasher);
    private final CreateCartaoRequest request = new CreateCartaoRequest("0123", "1234");

    @Test
    void createsCardWithInitialBalance() {
        when(hasher.hash("1234")).thenReturn("hash-simulado");
        when(repository.insert(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var card = service.create(request);
        assertThat(card.getNumeroCartao()).isEqualTo("0123");
        assertThat(card.getSenha()).isEqualTo("hash-simulado");
        assertThat(card.getSaldo()).isEqualTo(new BigDecimal("500.00"));
        var order = inOrder(repository);
        order.verify(repository).existsByCardNumber("0123");
        order.verify(repository).insert(card);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rejectsExistingCardWithoutWriting() {
        when(repository.existsByCardNumber("0123")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request))
            .isInstanceOfSatisfying(DuplicateCartaoException.class, exception -> {
                assertThat(exception.getNumeroCartao()).isEqualTo("0123");
                assertThat(exception.getSenha()).isEqualTo("1234");
            });
        verify(repository).existsByCardNumber("0123");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void propagatesDuplicateDetectedDuringInsert() {
        when(hasher.hash("1234")).thenReturn("hash-simulado");
        var error = new DuplicateCartaoException("0123", "hash-simulado");
        when(repository.insert(any())).thenThrow(error);
        assertThatThrownBy(() -> service.create(request))
            .isInstanceOfSatisfying(DuplicateCartaoException.class, exception -> {
                assertThat(exception.getNumeroCartao()).isEqualTo("0123");
                assertThat(exception.getSenha()).isEqualTo("1234");
            });
    }
}
