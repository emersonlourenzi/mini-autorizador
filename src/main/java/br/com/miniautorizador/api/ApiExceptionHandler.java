package br.com.miniautorizador.api;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.exceptions.cartao.CartaoNotFoundException;
import br.com.miniautorizador.exceptions.transacao.TransacaoNegadaException;
import org.springframework.http.MediaType;
import br.com.miniautorizador.model.cartao.response.CartaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

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
