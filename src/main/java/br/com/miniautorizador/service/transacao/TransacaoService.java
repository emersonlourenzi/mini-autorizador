package br.com.miniautorizador.service.transacao;

import br.com.miniautorizador.exceptions.transacao.TransacaoNegadaException;
import br.com.miniautorizador.model.cartao.Cartao;
import br.com.miniautorizador.model.transacao.request.TransacaoRequest;
import br.com.miniautorizador.repository.cartao.CartaoRepository;
import br.com.miniautorizador.service.security.PasswordHasher;
import br.com.miniautorizador.util.enums.MotivoNegacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final CartaoRepository repository;
    private final PasswordHasher passwordHasher;

    @Transactional
    public String authorize(TransacaoRequest request) {
        return repository.findByCardNumber(request.numeroCartao())
            .map(cartao -> validatePassword(cartao, request.senhaCartao()))
            .map(cartao -> validateBalanceAndDebit(cartao, request))
            .map(cartao -> "OK")
            .orElseThrow(() -> denied(MotivoNegacao.CARTAO_INEXISTENTE));
    }

    private Cartao validatePassword(Cartao cartao, String senha) {
        return Optional.of(cartao)
            .filter(value -> passwordHasher.matches(senha, value.getSenha()))
            .orElseThrow(() -> denied(MotivoNegacao.SENHA_INVALIDA));
    }

    private Cartao validateBalanceAndDebit(Cartao cartao, TransacaoRequest request) {
        return Optional.of(cartao)
            .filter(value -> repository.debitIfSufficientBalance(value.getNumeroCartao(), request.valor()))
            .orElseThrow(() -> denied(MotivoNegacao.SALDO_INSUFICIENTE));
    }

    private TransacaoNegadaException denied(MotivoNegacao motivo) {
        return new TransacaoNegadaException(motivo);
    }
}
