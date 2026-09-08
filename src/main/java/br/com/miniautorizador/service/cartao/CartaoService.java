package br.com.miniautorizador.service.cartao;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.exceptions.cartao.CartaoNotFoundException;
import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.model.cartao.request.CreateCartaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import br.com.miniautorizador.service.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository repository;
    private final PasswordHasher passwordHasher;

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

        try {
            return repository.insert(Cartao.create(validatedRequest.numeroCartao(), passwordHasher.hash(validatedRequest.senha())));
        } catch (DuplicateCartaoException exception) {
            throw new DuplicateCartaoException(validatedRequest.numeroCartao(), validatedRequest.senha());
        }
    }
}
