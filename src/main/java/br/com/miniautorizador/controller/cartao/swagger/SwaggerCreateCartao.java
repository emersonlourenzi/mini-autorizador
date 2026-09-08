package br.com.miniautorizador.controller.cartao.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Criar cartão", tags = {"Cartões"},
    description = "Cria um cartão com saldo de 500.00 e senha de quatro dígitos.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = br.com.miniautorizador.model.cartao.request.CreateCartaoRequest.class),
            examples = @ExampleObject(value = "{\"numeroCartao\":\"6549873025634501\",\"senha\":\"1234\"}"))),
    responses = {
        @ApiResponse(responseCode = "201", description = "Cartão criado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = br.com.miniautorizador.model.cartao.response.CartaoResponse.class),
                examples = @ExampleObject(value = "{\"senha\":\"1234\",\"numeroCartao\":\"6549873025634501\"}"))),
        @ApiResponse(responseCode = "422", description = "Cartão existente; retorna os dados enviados sem alterar o cadastro",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = br.com.miniautorizador.model.cartao.response.CartaoResponse.class),
                examples = @ExampleObject(value = "{\"senha\":\"1234\",\"numeroCartao\":\"6549873025634501\"}"))),
        @ApiResponse(responseCode = "400", description = "Campos inválidos; JSON malformado também retorna 400, com corpo padrão do Spring",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = br.com.miniautorizador.model.common.response.ValidationErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"erros\":[{\"campo\":\"numeroCartao\",\"mensagem\":\"O número do cartão é obrigatório.\"}]}")))
    })
public @interface SwaggerCreateCartao {
}
