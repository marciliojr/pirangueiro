package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Historico;
import com.marciliojr.pirangueiro.model.Usuario;
import com.marciliojr.pirangueiro.repository.HistoricoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository historicoRepository;

    public List<Historico> listarTodos() {
        return historicoRepository.findAll();
    }

    public List<Historico> buscarPorUsuario(Usuario usuario) {
        return historicoRepository.findByUsuarioOrderByDataHoraDesc(usuario);
    }

    public List<Historico> buscarPorEntidade(String entidade, Long entidadeId) {
        return historicoRepository.findByEntidadeAndEntidadeIdOrderByDataHoraDesc(entidade, entidadeId);
    }

    public List<Historico> buscarPorUsuarioEPeriodo(Long usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        return historicoRepository.findByUsuarioAndPeriodo(usuarioId, dataInicio, dataFim);
    }

    public Historico registrarOperacao(Historico.TipoOperacao tipoOperacao, String entidade, Long entidadeId, Usuario usuario) {
        Historico historico = new Historico();
        historico.setTipoOperacao(tipoOperacao);
        historico.setEntidade(entidade);
        historico.setEntidadeId(entidadeId);
        historico.setUsuario(usuario);
        
        return historicoRepository.save(historico);
    }

    public void registrarCriacaoDespesa(Long despesaId, Usuario usuario) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_DESPESA, "DESPESA", despesaId, usuario);
    }

    public void registrarCriacaoReceita(Long receitaId, Usuario usuario) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_RECEITA, "RECEITA", receitaId, usuario);
    }

    public void registrarCriacaoConta(Long contaId, Usuario usuario) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CONTA, "CONTA", contaId, usuario);
    }

    public void registrarCriacaoCartao(Long cartaoId, Usuario usuario) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CARTAO, "CARTAO", cartaoId, usuario);
    }

    public void registrarEdicaoDespesa(Long despesaId, Usuario usuario) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_DESPESA, "DESPESA", despesaId, usuario);
    }

    public void registrarEdicaoReceita(Long receitaId, Usuario usuario) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_RECEITA, "RECEITA", receitaId, usuario);
    }

    public void registrarEdicaoConta(Long contaId, Usuario usuario) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CONTA, "CONTA", contaId, usuario);
    }

    public void registrarEdicaoCartao(Long cartaoId, Usuario usuario) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CARTAO, "CARTAO", cartaoId, usuario);
    }
} 