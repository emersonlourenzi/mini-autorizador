package br.com.miniautorizador.repository.cartao;

import br.com.miniautorizador.model.cartao.Cartao;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CartaoPersistenceAdapterTest {

    @Autowired
    private CartaoRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void insertsAndReadsCardFromDatabase() {
        var cartao = Cartao.create(cardNumber(), "0123");

        var inserted = repository.insert(cartao);
        entityManager.clear();

        assertThat(inserted).usingRecursiveComparison().isEqualTo(cartao);
        assertThat(repository.existsByCardNumber(cartao.getNumeroCartao())).isTrue();
        assertThat(repository.findByCardNumber(cartao.getNumeroCartao())).hasValueSatisfying(found ->
            assertThat(found).usingRecursiveComparison().isEqualTo(cartao));
    }

    @Test
    void readsPersistedBalanceWithoutResettingIt() {
        var cartao = Cartao.restore(cardNumber(), "1234", new BigDecimal("495.15"));
        repository.insert(cartao);
        entityManager.clear();

        assertThat(repository.findByCardNumber(cartao.getNumeroCartao())).hasValueSatisfying(found ->
            assertThat(found.getSaldo()).isEqualTo(new BigDecimal("495.15")));
    }

    @Test
    void returnsEmptyForUnknownCard() {
        var number = cardNumber();

        assertThat(repository.existsByCardNumber(number)).isFalse();
        assertThat(repository.findByCardNumber(number)).isEmpty();
    }

    @Test
    void rejectsDuplicateWithoutOverwritingPasswordOrBalance() {
        var number = cardNumber();
        repository.insert(Cartao.restore(number, "0123", new BigDecimal("495.15")));
        entityManager.clear();

        assertThatThrownBy(() -> repository.insert(Cartao.create(number, "9876")))
            .isInstanceOf(DataIntegrityViolationException.class);

        var persisted = jdbcTemplate.queryForMap(
            "select senha, saldo from cartao where numero_cartao = ?", number);
        assertThat(persisted.get("senha")).isEqualTo("0123");
        assertThat(persisted.get("saldo")).isEqualTo(new BigDecimal("495.15"));
    }

    private String cardNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
