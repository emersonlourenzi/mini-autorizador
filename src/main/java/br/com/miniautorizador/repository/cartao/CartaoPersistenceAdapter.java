package br.com.miniautorizador.repository.cartao;

import br.com.miniautorizador.exceptions.cartao.DuplicateCartaoException;
import br.com.miniautorizador.mapper.cartao.CartaoMapper;
import br.com.miniautorizador.model.cartao.Cartao;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CartaoPersistenceAdapter implements CartaoRepository {

    private final JpaCartaoRepository repository;

    @Override
    @Transactional
    public Cartao insert(Cartao cartao) {
        try {
            repository.insert(cartao.getNumeroCartao(), cartao.getSenha(), cartao.getSaldo());
            return cartao;
        } catch (DataIntegrityViolationException exception) {
            throw Optional.of(exception.getMostSpecificCause())
                .filter(SQLException.class::isInstance)
                .map(SQLException.class::cast)
                .filter(cause -> cause.getErrorCode() == 1062 && "23000".equals(cause.getSQLState()))
                .<RuntimeException>map(cause -> new DuplicateCartaoException(cartao.getNumeroCartao(), cartao.getSenha()))
                .orElse(exception);
        }
    }

    @Override
    public boolean existsByCardNumber(String numeroCartao) {
        return repository.existsById(numeroCartao);
    }

    @Override
    public Optional<Cartao> findByCardNumber(String numeroCartao) {
        return repository.findById(numeroCartao)
            .map(CartaoMapper::toModel);
    }
}
