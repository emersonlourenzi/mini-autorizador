package br.com.miniautorizador.repository.cartao;

import br.com.miniautorizador.repository.cartao.entity.CartaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaCartaoRepository extends JpaRepository<CartaoEntity, String> {
}
