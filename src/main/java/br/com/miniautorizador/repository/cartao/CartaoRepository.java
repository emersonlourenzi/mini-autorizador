package br.com.miniautorizador.repository.cartao;

import br.com.miniautorizador.model.cartao.Cartao;

import java.math.BigDecimal;
import java.util.Optional;

public interface CartaoRepository {

    Cartao insert(Cartao cartao);

    boolean existsByCardNumber(String numeroCartao);

    Optional<Cartao> findByCardNumber(String numeroCartao);

    boolean debitIfSufficientBalance(String numeroCartao, BigDecimal valor);
}
