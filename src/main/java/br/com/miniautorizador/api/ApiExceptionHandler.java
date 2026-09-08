package br.com.miniautorizador.api;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.model.cartao.response.CartaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateCartaoException.class)
    public ResponseEntity<CartaoResponse> duplicateCard(DuplicateCartaoException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
            .body(new CartaoResponse(exception.getSenha(), exception.getNumeroCartao()));
    }
}
