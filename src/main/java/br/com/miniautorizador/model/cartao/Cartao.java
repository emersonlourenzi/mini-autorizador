package br.com.miniautorizador.model.cartao;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public final class Cartao {

    public static final BigDecimal SALDO_INICIAL = new BigDecimal("500.00");

    private final String numeroCartao;
    private final String senha;
    private final BigDecimal saldo;

    private Cartao(String numeroCartao, String senha, BigDecimal saldo) {
        this.numeroCartao = Objects.requireNonNull(numeroCartao, "numeroCartao must not be null");
        this.senha = Objects.requireNonNull(senha, "senha must not be null");
        this.saldo = Objects.requireNonNull(saldo, "saldo must not be null");
    }

    public static Cartao create(String numeroCartao, String senha) {
        return new Cartao(numeroCartao, senha, SALDO_INICIAL);
    }

    public static Cartao restore(String numeroCartao, String senha, BigDecimal saldo) {
        return new Cartao(numeroCartao, senha, saldo);
    }
}
