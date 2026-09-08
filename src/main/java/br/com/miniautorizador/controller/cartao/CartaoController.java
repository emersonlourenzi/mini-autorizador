package br.com.miniautorizador.controller.cartao;

import br.com.miniautorizador.controller.cartao.swagger.SwaggerCreateCartao;
import br.com.miniautorizador.controller.cartao.swagger.SwaggerReadBalance;
import br.com.miniautorizador.model.cartao.request.CreateCartaoRequest;
import br.com.miniautorizador.model.cartao.response.CartaoResponse;
import br.com.miniautorizador.service.cartao.CartaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static br.com.miniautorizador.mapper.cartao.CartaoMapper.toResponse;

@RestController
@RequestMapping("/cartoes")
@RequiredArgsConstructor
public class CartaoController {

    private final CartaoService service;

    @SwaggerReadBalance
    @GetMapping("/{numeroCartao}")
    public BigDecimal findBalance(@PathVariable String numeroCartao) {
        return service.findByCardNumber(numeroCartao).getSaldo();
    }

    @SwaggerCreateCartao
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartaoResponse create(@Valid @RequestBody CreateCartaoRequest request) {
        return toResponse(service.create(request), request.senha());
    }
}
