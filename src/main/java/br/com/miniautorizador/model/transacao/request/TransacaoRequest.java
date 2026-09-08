package br.com.miniautorizador.model.transacao.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransacaoRequest(
    @NotBlank(message = "O número do cartão é obrigatório.")
    @Size(max = 32, message = "O número do cartão deve conter no máximo 32 caracteres.") String numeroCartao,
    @NotBlank(message = "A senha do cartão é obrigatória.")
    @Size(max = 100, message = "A senha do cartão deve conter no máximo 100 caracteres.") String senhaCartao,
    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior ou igual a 0.01.")
    @Digits(integer = 17, fraction = 2, message = "O valor deve conter no máximo 17 dígitos inteiros e 2 casas decimais.") BigDecimal valor
) {
}
