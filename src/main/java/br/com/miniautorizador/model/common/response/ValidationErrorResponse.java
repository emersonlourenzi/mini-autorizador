package br.com.miniautorizador.model.common.response;

import java.util.List;

public record ValidationErrorResponse(int status, List<FieldErrorResponse> erros) {
    public record FieldErrorResponse(String campo, String mensagem) {
    }
}
