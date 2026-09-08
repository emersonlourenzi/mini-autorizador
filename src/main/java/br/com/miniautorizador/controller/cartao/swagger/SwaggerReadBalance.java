package br.com.miniautorizador.controller.cartao.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Consultar saldo", tags = {"Cartões"},
    parameters = @io.swagger.v3.oas.annotations.Parameter(name = "numeroCartao", required = true,
        description = "Número do cartão", example = "6549873025634501"),
    responses = {
        @ApiResponse(responseCode = "200", description = "Saldo disponível",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "number", example = "495.15"))),
        @ApiResponse(responseCode = "404", description = "Cartão inexistente", content = @Content)
    })
public @interface SwaggerReadBalance {
}
