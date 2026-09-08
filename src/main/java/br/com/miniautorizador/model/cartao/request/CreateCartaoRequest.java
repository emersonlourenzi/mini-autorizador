package br.com.miniautorizador.model.cartao.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCartaoRequest(
    @NotBlank @Size(max = 32) String numeroCartao,
    @NotBlank @Size(max = 100) String senha
) {
}
