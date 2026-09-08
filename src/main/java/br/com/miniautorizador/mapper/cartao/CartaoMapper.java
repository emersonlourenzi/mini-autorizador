package br.com.miniautorizador.mapper.cartao;

import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.model.cartao.response.CartaoResponse;
import br.com.miniautorizador.repository.cartao.entity.CartaoEntity;

public interface CartaoMapper {

    static CartaoResponse toResponse(Cartao value, String submittedPassword) {
        return new CartaoResponse(submittedPassword, value.getNumeroCartao());
    }

    static Cartao toModel(CartaoEntity value) {
        return Cartao.restore(value.getNumeroCartao(), value.getSenha(), value.getSaldo());
    }
}
