package br.com.miniautorizador.service.cartao;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.model.cartao.request.CreateCartaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository repository;

    @Transactional
    public Cartao create(CreateCartaoRequest request) {
        if (repository.existsByCardNumber(request.numeroCartao())) {
            throw new DuplicateCartaoException(request.numeroCartao(), request.senha());
        }
        return repository.insert(Cartao.create(request.numeroCartao(), request.senha()));
    }
}
