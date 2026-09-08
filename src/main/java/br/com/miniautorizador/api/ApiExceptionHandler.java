package br.com.miniautorizador.api;

import br.com.miniautorizador.exceptions.cartao.CartaoNotFoundException;
import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.exceptions.transacao.TransacaoNegadaException;
import br.com.miniautorizador.model.cartao.response.CartaoResponse;
import br.com.miniautorizador.model.common.response.ValidationErrorResponse.FieldErrorResponse;
import br.com.miniautorizador.model.common.response.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> invalidRequest(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
            .distinct()
            .sorted(java.util.Comparator.comparing(FieldErrorResponse::campo)
                .thenComparing(FieldErrorResponse::mensagem))
            .toList();
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(400, errors));
    }

    @ExceptionHandler(TransacaoNegadaException.class)
    public ResponseEntity<String> transactionDenied(TransacaoNegadaException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
            .contentType(MediaType.TEXT_PLAIN)
            .body(exception.getMotivo().name());
    }

    @ExceptionHandler(CartaoNotFoundException.class)
    public ResponseEntity<Void> cardNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DuplicateCartaoException.class)
    public ResponseEntity<CartaoResponse> duplicateCard(DuplicateCartaoException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
            .body(new CartaoResponse(exception.getSenha(), exception.getNumeroCartao()));
    }
}
