package br.com.miniautorizador.exceptions.transacao;

import br.com.miniautorizador.util.enums.MotivoNegacao;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TransacaoNegadaException extends RuntimeException {
    private final MotivoNegacao motivo;
}
