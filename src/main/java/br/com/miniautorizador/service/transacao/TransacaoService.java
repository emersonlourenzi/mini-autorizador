package br.com.miniautorizador.service.transacao;

import br.com.miniautorizador.exceptions.transacao.TransacaoNegadaException;
import br.com.miniautorizador.model.transacao.request.TransacaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import br.com.miniautorizador.util.enums.MotivoNegacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final CartaoRepository repository;

    @Transactional
    public void authorize(TransacaoRequest request) {
        var cartao = repository.findByCardNumber(request.numeroCartao())
            .orElseThrow(() -> new TransacaoNegadaException(MotivoNegacao.CARTAO_INEXISTENTE));

        if (!cartao.getSenha().equals(request.senhaCartao())) {
            throw new TransacaoNegadaException(MotivoNegacao.SENHA_INVALIDA);
        }
        if (cartao.getSaldo().compareTo(request.valor()) < 0) {
            throw new TransacaoNegadaException(MotivoNegacao.SALDO_INSUFICIENTE);
        }

        repository.updateBalance(cartao.getNumeroCartao(), cartao.getSaldo().subtract(request.valor()));
    }

}
