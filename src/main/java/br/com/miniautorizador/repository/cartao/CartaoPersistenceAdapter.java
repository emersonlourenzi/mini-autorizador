package br.com.miniautorizador.repository.cartao;

import br.com.miniautorizador.mapper.cartao.CartaoMapper;
import br.com.miniautorizador.model.cartao.Cartao;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CartaoPersistenceAdapter implements CartaoRepository {

    private final JpaCartaoRepository repository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Cartao insert(Cartao cartao) {
        var entity = CartaoMapper.toEntity(cartao);
        entityManager.persist(entity);
        entityManager.flush();
        return CartaoMapper.toModel(entity);
    }

    @Override
    public boolean existsByCardNumber(String numeroCartao) {
        return repository.existsById(numeroCartao);
    }

    @Override
    public Optional<Cartao> findByCardNumber(String numeroCartao) {
        return repository.findById(numeroCartao).map(CartaoMapper::toModel);
    }
}
