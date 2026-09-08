package br.com.miniautorizador.service.cartao;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.model.cartao.request.CreateCartaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartaoServiceTest {
    private final CartaoRepository repository = mock(CartaoRepository.class);
    private final CartaoService service = new CartaoService(repository);
    private final CreateCartaoRequest request = new CreateCartaoRequest("0123", "1234");

    @Test
    void createsCardWithInitialBalance() {
        when(repository.insert(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var card = service.create(request);
        assertThat(card.getNumeroCartao()).isEqualTo("0123");
        assertThat(card.getSenha()).isEqualTo("1234");
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
        var error = new DuplicateCartaoException("0123", "1234");
        when(repository.insert(any())).thenThrow(error);
        assertThatThrownBy(() -> service.create(request)).isSameAs(error);
    }
}
