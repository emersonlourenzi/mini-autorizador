package br.com.miniautorizador.repository.cartao;

import br.com.miniautorizador.repository.cartao.entity.CartaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

interface JpaCartaoRepository extends JpaRepository<CartaoEntity, String> {

    @Modifying
    @Query(value = """
        INSERT INTO cartao (numero_cartao, senha, saldo)
        VALUES (:numeroCartao, :senha, :saldo)
        """, nativeQuery = true)
    void insert(String numeroCartao, String senha, BigDecimal saldo);
}
