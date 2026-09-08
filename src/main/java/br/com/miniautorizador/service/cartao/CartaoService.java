package br.com.miniautorizador.service.cartao;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.exceptions.cartao.CartaoNotFoundException;
import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.model.cartao.request.CreateCartaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository repository;

    @Transactional(readOnly = true)
    public Cartao findByCardNumber(String numeroCartao) {
        return repository.findByCardNumber(numeroCartao)
            .orElseThrow(CartaoNotFoundException::new);
    }

    @Transactional
    public Cartao create(CreateCartaoRequest request) {
        var validatedRequest = Optional.of(request)
            .filter(value -> !repository.existsByCardNumber(value.numeroCartao()))
            .orElseThrow(() -> new DuplicateCartaoException(request.numeroCartao(), request.senha()));

        return repository.insert(Cartao.create(validatedRequest.numeroCartao(), validatedRequest.senha()));
    }
}
