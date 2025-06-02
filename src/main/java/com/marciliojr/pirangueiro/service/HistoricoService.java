package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Historico;
import com.marciliojr.pirangueiro.repository.HistoricoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository historicoRepository;

    public List<Historico> listarTodos() {
        return historicoRepository.findAll();
    }

    public List<Historico> buscarPorEntidade(String entidade, Long entidadeId) {
        return historicoRepository.findByEntidadeAndEntidadeIdOrderByDataHoraDesc(entidade, entidadeId);
    }

    public Historico registrarOperacao(Historico.TipoOperacao tipoOperacao, String entidade, Long entidadeId) {
        Historico historico = new Historico();
        historico.setTipoOperacao(tipoOperacao);
        historico.setEntidade(entidade);
        historico.setEntidadeId(entidadeId);
        
        return historicoRepository.save(historico);
    }

    public void registrarCriacaoDespesa(Long despesaId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_DESPESA, "DESPESA", despesaId);
    }

    public void registrarCriacaoReceita(Long receitaId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_RECEITA, "RECEITA", receitaId);
    }

    public void registrarCriacaoConta(Long contaId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CONTA, "CONTA", contaId);
    }

    public void registrarCriacaoCartao(Long cartaoId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CARTAO, "CARTAO", cartaoId);
    }

    public void registrarEdicaoDespesa(Long despesaId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_DESPESA, "DESPESA", despesaId);
    }

    public void registrarEdicaoReceita(Long receitaId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_RECEITA, "RECEITA", receitaId);
    }

    public void registrarEdicaoConta(Long contaId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CONTA, "CONTA", contaId);
    }

    public void registrarEdicaoCartao(Long cartaoId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CARTAO, "CARTAO", cartaoId);
    }
} 