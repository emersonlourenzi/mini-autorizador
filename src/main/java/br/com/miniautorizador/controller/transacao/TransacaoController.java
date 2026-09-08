package br.com.miniautorizador.controller.transacao;

import br.com.miniautorizador.controller.transacao.swagger.SwaggerAuthorizeTransacao;
import br.com.miniautorizador.model.transacao.request.TransacaoRequest;
import br.com.miniautorizador.service.transacao.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService service;

    @SwaggerAuthorizeTransacao
    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public String authorize(@Valid @RequestBody TransacaoRequest request) {
        return service.authorize(request);
    }
}
