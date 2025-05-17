package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.model.ExecucaoTarefa;
import com.marciliojr.pirangueiro.model.Notificacao;
import com.marciliojr.pirangueiro.repository.CartaoRepository;
import com.marciliojr.pirangueiro.repository.ExecucaoTarefaRepository;
import com.marciliojr.pirangueiro.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private static final String TAREFA_VERIFICACAO_CARTOES = "VERIFICACAO_CARTOES";
    
    private final CartaoRepository cartaoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final ExecucaoTarefaRepository execucaoTarefaRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void verificarCartoesAoIniciar() {
        if (jaExecutouHoje()) {
            return;
        }
        acaoNotificarCartao();
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void verificarCartoesDiariamente() {
        acaoNotificarCartao();
    }


    private void acaoNotificarCartao() {
        LocalDate hoje = LocalDate.now();
        List<Cartao> cartoes = cartaoRepository.findAll();

        for (Cartao cartao : cartoes) {
            if (cartao.getDiaFechamento() == hoje.getDayOfMonth()) {
                criarNotificacao(cartao, "Hoje é o fechamento da fatura. Compras de agora irão para a próxima fatura.");
            }

            if (cartao.getDiaVencimento() == hoje.getDayOfMonth()) {
                criarNotificacao(cartao, "Hoje é o vencimento da fatura. Lembre-se de pagá-la.");
            }
        }

        registrarExecucao();
    }

    private boolean jaExecutouHoje() {
        return execucaoTarefaRepository
                .findByNomeTarefaAndDataExecucao(TAREFA_VERIFICACAO_CARTOES, LocalDate.now())
                .isPresent();
    }

    private void registrarExecucao() {
        ExecucaoTarefa execucao = ExecucaoTarefa.criar(TAREFA_VERIFICACAO_CARTOES);
        execucaoTarefaRepository.save(execucao);
    }

    private void criarNotificacao(Cartao cartao, String mensagem) {
        Notificacao notificacao = new Notificacao();
        notificacao.setCartao(cartao);
        notificacao.setMensagem(mensagem);
        notificacao.setDataGeracao(LocalDateTime.now());
        notificacao.setLida(false);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public List<Notificacao> buscarNotificacoesNaoLidas() {
        return notificacaoRepository.findByLidaFalseOrderByDataGeracaoDesc();
    }

    @Transactional
    public void marcarComoLida(Long notificacaoId) {
        notificacaoRepository.findById(notificacaoId).ifPresent(notificacao -> {
            notificacao.setLida(true);
            notificacaoRepository.save(notificacao);
        });
    }
} 