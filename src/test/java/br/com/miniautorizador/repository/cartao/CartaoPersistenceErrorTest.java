package br.com.miniautorizador.repository.cartao;

import br.com.miniautorizador.model.cartao.Cartao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class CartaoPersistenceErrorTest {

    private final JpaCartaoRepository repository = mock(JpaCartaoRepository.class);
    private final CartaoPersistenceAdapter adapter =
        new CartaoPersistenceAdapter(repository);

    @Test
    void doesNotTranslateOtherDatabaseErrorsAsDuplicate() {
        var error = new DataIntegrityViolationException("not null", new SQLException("not null", "23000", 1048));
        doThrow(error).when(repository).insert(any(), any(), any());

        assertThatThrownBy(() -> adapter.insert(Cartao.create("123", "0123"))).isSameAs(error);
    }

    @Test
    void preservesPersistenceErrorWithoutSqlCause() {
        var error = new DataIntegrityViolationException("integrity violation");
        doThrow(error).when(repository).insert(any(), any(), any());

        assertThatThrownBy(() -> adapter.insert(Cartao.create("123", "0123"))).isSameAs(error);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"HY000", "23505"})
    void preservesDuplicateCodeWithUnexpectedSqlState(String sqlState) {
        var error = new DataIntegrityViolationException("unexpected state",
            new SQLException("failure", sqlState, 1062));
        doThrow(error).when(repository).insert(any(), any(), any());

        assertThatThrownBy(() -> adapter.insert(Cartao.create("123", "0123"))).isSameAs(error);
    }
}
