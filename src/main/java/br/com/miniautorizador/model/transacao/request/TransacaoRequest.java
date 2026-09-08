package br.com.miniautorizador.model.transacao.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransacaoRequest(
    @NotBlank @Size(max = 32) String numeroCartao,
    @NotBlank @Size(max = 100) String senhaCartao,
    @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal valor
) {
}
