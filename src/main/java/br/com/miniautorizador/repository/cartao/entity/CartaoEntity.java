package br.com.miniautorizador.repository.cartao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cartao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CartaoEntity {

    @Id
    @Column(name = "numero_cartao", nullable = false, length = 32)
    private String numeroCartao;

    @Column(nullable = false, length = 100)
    private String senha;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;
}
