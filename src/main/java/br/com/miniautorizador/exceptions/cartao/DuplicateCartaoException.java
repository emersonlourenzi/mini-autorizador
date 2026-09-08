package br.com.miniautorizador.exceptions.cartao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DuplicateCartaoException extends RuntimeException {

    private final String numeroCartao;
    private final String senha;
}
