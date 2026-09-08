package br.com.miniautorizador.controller.transacao.swagger;

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
@Operation(summary = "Autorizar transação", tags = {"Transações"},
    description = "Verifica cartão e senha e realiza débito atômico quando há saldo suficiente.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = br.com.miniautorizador.model.transacao.request.TransacaoRequest.class),
            examples = @ExampleObject(value = "{\"numeroCartao\":\"6549873025634501\",\"senhaCartao\":\"1234\",\"valor\":10.00}"))),
    responses = {
        @ApiResponse(responseCode = "201", description = "Transação aprovada",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "OK"))),
        @ApiResponse(responseCode = "422", description = "Transação recusada",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string",
                allowableValues = {"CARTAO_INEXISTENTE", "SENHA_INVALIDA", "SALDO_INSUFICIENTE"}),
                examples = {@ExampleObject(name = "Cartão inexistente", value = "CARTAO_INEXISTENTE"),
                    @ExampleObject(name = "Senha inválida", value = "SENHA_INVALIDA"),
                    @ExampleObject(name = "Saldo insuficiente", value = "SALDO_INSUFICIENTE")})),
        @ApiResponse(responseCode = "400", description = "Campos inválidos; JSON malformado também retorna 400, com corpo padrão do Spring",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = br.com.miniautorizador.model.common.response.ValidationErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"erros\":[{\"campo\":\"numeroCartao\",\"mensagem\":\"O número do cartão é obrigatório.\"}]}")))
    })
public @interface SwaggerAuthorizeTransacao {
}
