package br.com.miniautorizador.model.cartao.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record CreateCartaoRequest(
    @NotBlank(message = "O número do cartão é obrigatório.")
    @Size(max = 32, message = "O número do cartão deve conter no máximo 32 caracteres.") String numeroCartao,
    @NotBlank(message = "A senha é obrigatória.")
    @Pattern(regexp = "[0-9]{4}", message = "A senha deve conter exatamente 4 dígitos numéricos.") String senha
) {
}
