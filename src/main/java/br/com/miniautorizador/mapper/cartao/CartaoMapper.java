package br.com.miniautorizador.mapper.cartao;

import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.repository.cartao.entity.CartaoEntity;

public interface CartaoMapper {

    static CartaoEntity toEntity(Cartao value) {
        return new CartaoEntity(value.getNumeroCartao(), value.getSenha(), value.getSaldo());
    }

    static Cartao toModel(CartaoEntity value) {
        return Cartao.restore(value.getNumeroCartao(), value.getSenha(), value.getSaldo());
    }
}
