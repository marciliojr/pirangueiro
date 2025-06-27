package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Historico;
import com.marciliojr.pirangueiro.model.Usuario;
import com.marciliojr.pirangueiro.repository.HistoricoRepository;
import com.marciliojr.pirangueiro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;



    /**
     * Registra uma operação no histórico do sistema
     * @param tipoOperacao Tipo da operação realizada
     * @param entidade Nome da entidade
     * @param entidadeId ID da entidade
     * @param entidadeInfo toString() da entidade para histórico completo
     * @param usuarioId ID do usuário que realizou a operação (pode ser null se não houver usuário logado)
     * @return Historico salvo
     */
    public Historico registrarOperacao(Historico.TipoOperacao tipoOperacao, String entidade, Long entidadeId, 
                                      String entidadeInfo, Long usuarioId) {
        Historico historico = new Historico();
        historico.setTipoOperacao(tipoOperacao);
        historico.setEntidade(entidade);
        historico.setEntidadeId(entidadeId);
        historico.setInfo(entidadeInfo);
        
        // Buscar e associar o usuário se o ID foi fornecido
        if (usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            historico.setUsuario(usuario);
        }
        
        return historicoRepository.save(historico);
    }

    /**
     * Versão simplificada que mantém compatibilidade com código existente
     */
    public Historico registrarOperacao(Historico.TipoOperacao tipoOperacao, String entidade, Long entidadeId) {
        return registrarOperacao(tipoOperacao, entidade, entidadeId, null, null);
    }

    // ==================== MÉTODOS PARA DESPESAS ====================
    
    public void registrarCriacaoDespesa(Long despesaId, String despesaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_DESPESA, "DESPESA", despesaId, despesaInfo, usuarioId);
    }

    public void registrarEdicaoDespesa(Long despesaId, String despesaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_DESPESA, "DESPESA", despesaId, despesaInfo, usuarioId);
    }

    public void registrarExclusaoDespesa(Long despesaId, String despesaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_DESPESA, "DESPESA", despesaId, despesaInfo, usuarioId);
    }

    // Métodos antigos mantidos para compatibilidade
    public void registrarCriacaoDespesa(Long despesaId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_DESPESA, "DESPESA", despesaId);
    }

    public void registrarEdicaoDespesa(Long despesaId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_DESPESA, "DESPESA", despesaId);
    }

    // ==================== MÉTODOS PARA RECEITAS ====================
    
    public void registrarCriacaoReceita(Long receitaId, String receitaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_RECEITA, "RECEITA", receitaId, receitaInfo, usuarioId);
    }

    public void registrarEdicaoReceita(Long receitaId, String receitaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_RECEITA, "RECEITA", receitaId, receitaInfo, usuarioId);
    }

    public void registrarExclusaoReceita(Long receitaId, String receitaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_RECEITA, "RECEITA", receitaId, receitaInfo, usuarioId);
    }

    // Métodos antigos mantidos para compatibilidade
    public void registrarCriacaoReceita(Long receitaId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_RECEITA, "RECEITA", receitaId);
    }

    public void registrarEdicaoReceita(Long receitaId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_RECEITA, "RECEITA", receitaId);
    }

    // ==================== MÉTODOS PARA CONTAS ====================
    
    public void registrarCriacaoConta(Long contaId, String contaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CONTA, "CONTA", contaId, contaInfo, usuarioId);
    }

    public void registrarEdicaoConta(Long contaId, String contaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CONTA, "CONTA", contaId, contaInfo, usuarioId);
    }

    public void registrarExclusaoConta(Long contaId, String contaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_CONTA, "CONTA", contaId, contaInfo, usuarioId);
    }

    // Métodos antigos mantidos para compatibilidade
    public void registrarCriacaoConta(Long contaId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CONTA, "CONTA", contaId);
    }

    public void registrarEdicaoConta(Long contaId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CONTA, "CONTA", contaId);
    }

    // ==================== MÉTODOS PARA CARTÕES ====================
    
    public void registrarCriacaoCartao(Long cartaoId, String cartaoInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CARTAO, "CARTAO", cartaoId, cartaoInfo, usuarioId);
    }

    public void registrarEdicaoCartao(Long cartaoId, String cartaoInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CARTAO, "CARTAO", cartaoId, cartaoInfo, usuarioId);
    }

    public void registrarExclusaoCartao(Long cartaoId, String cartaoInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_CARTAO, "CARTAO", cartaoId, cartaoInfo, usuarioId);
    }

    // Métodos antigos mantidos para compatibilidade
    public void registrarCriacaoCartao(Long cartaoId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CARTAO, "CARTAO", cartaoId);
    }

    public void registrarEdicaoCartao(Long cartaoId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CARTAO, "CARTAO", cartaoId);
    }

    // ==================== MÉTODOS PARA CATEGORIAS ====================
    
    public void registrarCriacaoCategoria(Long categoriaId, String categoriaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_CATEGORIA, "CATEGORIA", categoriaId, categoriaInfo, usuarioId);
    }

    public void registrarEdicaoCategoria(Long categoriaId, String categoriaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_CATEGORIA, "CATEGORIA", categoriaId, categoriaInfo, usuarioId);
    }

    public void registrarExclusaoCategoria(Long categoriaId, String categoriaInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_CATEGORIA, "CATEGORIA", categoriaId, categoriaInfo, usuarioId);
    }

    // ==================== MÉTODOS PARA USUÁRIOS ====================
    
    public void registrarCriacaoUsuario(Long usuarioId, String usuarioInfo, Long usuarioLogadoId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_USUARIO, "USUARIO", usuarioId, usuarioInfo, usuarioLogadoId);
    }

    public void registrarEdicaoUsuario(Long usuarioId, String usuarioInfo, Long usuarioLogadoId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_USUARIO, "USUARIO", usuarioId, usuarioInfo, usuarioLogadoId);
    }

    public void registrarExclusaoUsuario(Long usuarioId, String usuarioInfo, Long usuarioLogadoId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_USUARIO, "USUARIO", usuarioId, usuarioInfo, usuarioLogadoId);
    }

    // ==================== MÉTODOS PARA PENSAMENTOS ====================
    
    public void registrarCriacaoPensamentos(Long pensamentosId, String pensamentosInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_PENSAMENTOS, "PENSAMENTOS", pensamentosId, pensamentosInfo, usuarioId);
    }

    public void registrarEdicaoPensamentos(Long pensamentosId, String pensamentosInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_PENSAMENTOS, "PENSAMENTOS", pensamentosId, pensamentosInfo, usuarioId);
    }

    public void registrarExclusaoPensamentos(Long pensamentosId, String pensamentosInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_PENSAMENTOS, "PENSAMENTOS", pensamentosId, pensamentosInfo, usuarioId);
    }

    // ==================== MÉTODOS PARA LIMITE DE GASTOS ====================
    
    public void registrarCriacaoLimiteGastos(Long limiteGastosId, String limiteGastosInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_LIMITE_GASTOS, "LIMITE_GASTOS", limiteGastosId, limiteGastosInfo, usuarioId);
    }

    public void registrarEdicaoLimiteGastos(Long limiteGastosId, String limiteGastosInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_LIMITE_GASTOS, "LIMITE_GASTOS", limiteGastosId, limiteGastosInfo, usuarioId);
    }

    public void registrarExclusaoLimiteGastos(Long limiteGastosId, String limiteGastosInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_LIMITE_GASTOS, "LIMITE_GASTOS", limiteGastosId, limiteGastosInfo, usuarioId);
    }

    // ==================== MÉTODOS PARA NOTIFICAÇÕES ====================
    
    public void registrarCriacaoNotificacao(Long notificacaoId, String notificacaoInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.CRIACAO_NOTIFICACAO, "NOTIFICACAO", notificacaoId, notificacaoInfo, usuarioId);
    }

    public void registrarEdicaoNotificacao(Long notificacaoId, String notificacaoInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EDICAO_NOTIFICACAO, "NOTIFICACAO", notificacaoId, notificacaoInfo, usuarioId);
    }

    public void registrarExclusaoNotificacao(Long notificacaoId, String notificacaoInfo, Long usuarioId) {
        registrarOperacao(Historico.TipoOperacao.EXCLUSAO_NOTIFICACAO, "NOTIFICACAO", notificacaoId, notificacaoInfo, usuarioId);
    }
} 