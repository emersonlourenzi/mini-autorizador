package br.com.miniautorizador.model.cartao;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CartaoTest {

    @Test
    void createsCardWithInitialBalanceAndPreservesLeadingZeros() {
        var cartao = Cartao.create("0123456789012345", "0123");

        assertThat(cartao.getNumeroCartao()).isEqualTo("0123456789012345");
        assertThat(cartao.getSenha()).isEqualTo("0123");
        assertThat(cartao.getSaldo()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void restoresExistingBalanceWithoutResettingIt() {
        var cartao = Cartao.restore("0123456789012345", "0123", new BigDecimal("495.15"));

        assertThat(cartao.getSaldo()).isEqualTo(new BigDecimal("495.15"));
    }
}
