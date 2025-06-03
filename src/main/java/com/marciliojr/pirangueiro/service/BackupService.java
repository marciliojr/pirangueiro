package com.marciliojr.pirangueiro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marciliojr.pirangueiro.dto.*;
import com.marciliojr.pirangueiro.model.*;
import com.marciliojr.pirangueiro.repository.*;
import com.marciliojr.pirangueiro.model.StatusImportacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo backup e restauração de dados do sistema.
 */
@Service
public class BackupService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private ContaRepository contaRepository;
    
    @Autowired
    private CartaoRepository cartaoRepository;
    
    @Autowired
    private PensamentosRepository pensamentosRepository;
    
    @Autowired
    private LimiteGastosRepository limiteGastosRepository;
    
    @Autowired
    private GraficosRepository graficosRepository;
    
    @Autowired
    private ExecucaoTarefaRepository execucaoTarefaRepository;
    
    @Autowired
    private DespesaRepository despesaRepository;
    
    @Autowired
    private ReceitaRepository receitaRepository;
    
    @Autowired
    private NotificacaoRepository notificacaoRepository;
    
    @Autowired
    private HistoricoRepository historicoRepository;
    
    @Autowired
    private StatusImportacaoRepository statusImportacaoRepository;

    private final ObjectMapper objectMapper;

    public BackupService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Obtém o status de uma importação pelo requestId
     */
    public Map<String, Object> obterStatusImportacao(String requestId) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            StatusImportacao statusImportacao = statusImportacaoRepository.findById(requestId).orElse(null);
            
            if (statusImportacao == null) {
                status.put("encontrado", false);
                status.put("erro", "Importação não encontrada");
                return status;
            }
            
            status.put("encontrado", true);
            status.put("requestId", statusImportacao.getRequestId());
            status.put("status", statusImportacao.getStatus().toString());
            status.put("mensagem", statusImportacao.getMensagem());
            status.put("dataInicio", statusImportacao.getDataInicio());
            status.put("dataFinalizacao", statusImportacao.getDataFinalizacao());
            status.put("nomeArquivo", statusImportacao.getNomeArquivo());
            status.put("totalRegistros", statusImportacao.getTotalRegistros());
            status.put("versaoBackup", statusImportacao.getVersaoBackup());
            status.put("detalhesErro", statusImportacao.getDetalhesErro());
            
            // Calcular tempo decorrido
            if (statusImportacao.getDataFinalizacao() != null) {
                long segundos = java.time.Duration.between(statusImportacao.getDataInicio(), 
                    statusImportacao.getDataFinalizacao()).getSeconds();
                status.put("tempoProcessamento", segundos + " segundos");
            } else {
                long segundos = java.time.Duration.between(statusImportacao.getDataInicio(), 
                    LocalDateTime.now()).getSeconds();
                status.put("tempoDecorrido", segundos + " segundos");
            }
            
        } catch (Exception e) {
            status.put("encontrado", false);
            status.put("erro", "Erro ao obter status: " + e.getMessage());
        }
        
        return status;
    }

    /**
     * Atualiza o status de uma importação
     */
    public void atualizarStatusImportacao(String requestId, String status, String mensagem) {
        try {
            StatusImportacao statusImportacao = statusImportacaoRepository.findById(requestId).orElse(null);
            if (statusImportacao != null) {
                StatusImportacao.StatusEnum statusEnum = StatusImportacao.StatusEnum.valueOf(status);
                statusImportacao.atualizar(statusEnum, mensagem);
                statusImportacaoRepository.save(statusImportacao);
            }
        } catch (Exception e) {
            // Log do erro, mas não quebra o fluxo
            System.err.println("Erro ao atualizar status da importação " + requestId + ": " + e.getMessage());
        }
    }

    /**
     * Lista todas as importações recentes (últimas 24 horas)
     */
    public List<Map<String, Object>> listarImportacoesRecentes() {
        LocalDateTime ontemMesmoHorario = LocalDateTime.now().minusHours(24);
        List<StatusImportacao> importacoes = statusImportacaoRepository
            .findByDataInicioAfterOrderByDataInicioDesc(ontemMesmoHorario);
        
        return importacoes.stream().map(this::converterStatusParaMap).collect(Collectors.toList());
    }

    /**
     * Lista todas as importações
     */
    public List<Map<String, Object>> listarTodasImportacoes() {
        List<StatusImportacao> importacoes = statusImportacaoRepository.findAllOrderByDataInicioDesc();
        return importacoes.stream().map(this::converterStatusParaMap).collect(Collectors.toList());
    }

    /**
     * Converte StatusImportacao para Map
     */
    private Map<String, Object> converterStatusParaMap(StatusImportacao status) {
        Map<String, Object> map = new HashMap<>();
        map.put("requestId", status.getRequestId());
        map.put("status", status.getStatus().toString());
        map.put("mensagem", status.getMensagem());
        map.put("dataInicio", status.getDataInicio());
        map.put("dataFinalizacao", status.getDataFinalizacao());
        map.put("nomeArquivo", status.getNomeArquivo());
        map.put("totalRegistros", status.getTotalRegistros());
        map.put("versaoBackup", status.getVersaoBackup());
        map.put("detalhesErro", status.getDetalhesErro());
        return map;
    }

    /**
     * Limpa registros de status antigos (mais de 7 dias)
     */
    @Transactional
    public void limparStatusAntigos() {
        LocalDateTime seteDiasAtras = LocalDateTime.now().minusDays(7);
        List<StatusImportacao> statusAntigos = statusImportacaoRepository
            .findByDataInicioAfterOrderByDataInicioDesc(seteDiasAtras);
        
        if (!statusAntigos.isEmpty()) {
            statusImportacaoRepository.deleteAll(statusAntigos);
        }
    }

    /**
     * Gera um backup completo de todas as entidades do sistema.
     */
    public BackupDTO gerarBackupCompleto() {
        BackupDTO backup = new BackupDTO();
        
        // Metadados
        backup.setDataGeracao(LocalDateTime.now());
        backup.setVersao("1.0");
        backup.setSistemaVersao("Pirangueiro v1.0");
        
        // Entidades independentes
        backup.setUsuarios(converterUsuarios());
        backup.setCategorias(converterCategorias());
        backup.setContas(converterContas());
        backup.setCartoes(converterCartoes());
        backup.setPensamentos(converterPensamentos());
        backup.setLimitesGastos(converterLimitesGastos());
        backup.setGraficos(converterGraficos());
        backup.setExecucoesTarefas(converterExecucoesTarefas());
        
        // Entidades com relacionamentos
        backup.setDespesas(converterDespesas());
        backup.setReceitas(converterReceitas());
        backup.setNotificacoes(converterNotificacoes());
        backup.setHistoricos(converterHistoricos());
        
        // Calcular total de registros
        backup.setTotalRegistros(calcularTotalRegistros(backup));
        
        return backup;
    }

    /**
     * Serializa o backup para JSON.
     */
    public String serializarBackup(BackupDTO backup) throws IOException {
        return objectMapper.writeValueAsString(backup);
    }

    /**
     * Deserializa o backup a partir do JSON.
     */
    public BackupDTO deserializarBackup(String json) throws IOException {
        return objectMapper.readValue(json, BackupDTO.class);
    }

    /**
     * Deserializa o backup a partir de um arquivo.
     */
    public BackupDTO deserializarBackup(MultipartFile arquivo) throws IOException {
        return objectMapper.readValue(arquivo.getInputStream(), BackupDTO.class);
    }

    /**
     * Deserializa o backup a partir de um array de bytes.
     */
    public BackupDTO deserializarBackup(byte[] conteudoArquivo) throws IOException {
        return objectMapper.readValue(conteudoArquivo, BackupDTO.class);
    }

    /**
     * Restaura todo o sistema a partir de um backup.
     * ATENÇÃO: Este método apaga todos os dados existentes!
     */
    @Transactional
    public void restaurarBackup(BackupDTO backup) {
        // 1. Limpar todas as tabelas (ordem importante para respeitar relacionamentos)
        limparTodasTabelas();
        
        // 2. Restaurar entidades independentes primeiro e criar mapas de mapeamento
        Map<Long, Long> mapaUsuarios = restaurarUsuariosComMapeamento(backup.getUsuarios());
        Map<Long, Long> mapaCategorias = restaurarCategoriasComMapeamento(backup.getCategorias());
        Map<Long, Long> mapaContas = restaurarContasComMapeamento(backup.getContas());
        Map<Long, Long> mapaCartoes = restaurarCartoesComMapeamento(backup.getCartoes());
        
        restaurarPensamentos(backup.getPensamentos());
        restaurarLimitesGastos(backup.getLimitesGastos());
        restaurarGraficos(backup.getGraficos());
        restaurarExecucoesTarefas(backup.getExecucoesTarefas());
        
        // 3. Restaurar entidades com relacionamentos usando os mapas
        restaurarDespesasComMapeamento(backup.getDespesas(), mapaContas, mapaCartoes, mapaCategorias);
        restaurarReceitasComMapeamento(backup.getReceitas(), mapaContas, mapaCategorias);
        restaurarNotificacoesComMapeamento(backup.getNotificacoes(), mapaCartoes);
        restaurarHistoricosComMapeamento(backup.getHistoricos(), mapaUsuarios);
    }

    /**
     * Limpa todas as tabelas do banco de dados.
     * A ordem é importante para respeitar os relacionamentos.
     */
    @Transactional
    public void limparTodasTabelas() {
        // Primeiro: entidades com relacionamentos
        historicoRepository.deleteAll();
        notificacaoRepository.deleteAll();
        receitaRepository.deleteAll();
        despesaRepository.deleteAll();
        
        // Depois: entidades independentes
        execucaoTarefaRepository.deleteAll();
        graficosRepository.deleteAll();
        limiteGastosRepository.deleteAll();
        pensamentosRepository.deleteAll();
        cartaoRepository.deleteAll();
        contaRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    // Métodos de conversão para backup
    
    private List<UsuarioBackupDTO> converterUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::converterUsuario)
                .collect(Collectors.toList());
    }
    
    private UsuarioBackupDTO converterUsuario(Usuario usuario) {
        UsuarioBackupDTO dto = new UsuarioBackupDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setSenha(usuario.getSenha());
        return dto;
    }
    
    private List<CategoriaBackupDTO> converterCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::converterCategoria)
                .collect(Collectors.toList());
    }
    
    private CategoriaBackupDTO converterCategoria(Categoria categoria) {
        CategoriaBackupDTO dto = new CategoriaBackupDTO();
        dto.setId(categoria.getId());
        dto.setNome(categoria.getNome());
        dto.setCor(categoria.getCor());
        dto.setTipoReceita(categoria.getTipoReceita());
        return dto;
    }
    
    private List<ContaBackupDTO> converterContas() {
        return contaRepository.findAll().stream()
                .map(this::converterConta)
                .collect(Collectors.toList());
    }
    
    private ContaBackupDTO converterConta(Conta conta) {
        ContaBackupDTO dto = new ContaBackupDTO();
        dto.setId(conta.getId());
        dto.setNome(conta.getNome());
        dto.setTipo(conta.getTipo() != null ? conta.getTipo().toString() : null);
        dto.setImagemLogo(conta.getImagemLogo());
        return dto;
    }
    
    private List<CartaoBackupDTO> converterCartoes() {
        return cartaoRepository.findAll().stream()
                .map(this::converterCartao)
                .collect(Collectors.toList());
    }
    
    private CartaoBackupDTO converterCartao(Cartao cartao) {
        CartaoBackupDTO dto = new CartaoBackupDTO();
        dto.setId(cartao.getId());
        dto.setNome(cartao.getNome());
        dto.setLimite(cartao.getLimite());
        dto.setLimiteUsado(cartao.getLimiteUsado());
        dto.setDiaFechamento(cartao.getDiaFechamento());
        dto.setDiaVencimento(cartao.getDiaVencimento());
        return dto;
    }
    
    private List<PensamentosBackupDTO> converterPensamentos() {
        return pensamentosRepository.findAll().stream()
                .map(this::converterPensamento)
                .collect(Collectors.toList());
    }
    
    private PensamentosBackupDTO converterPensamento(Pensamentos pensamento) {
        PensamentosBackupDTO dto = new PensamentosBackupDTO();
        dto.setId(pensamento.getId());
        dto.setTexto(pensamento.getTexto());
        return dto;
    }
    
    private List<LimiteGastosBackupDTO> converterLimitesGastos() {
        return limiteGastosRepository.findAll().stream()
                .map(this::converterLimiteGastos)
                .collect(Collectors.toList());
    }
    
    private LimiteGastosBackupDTO converterLimiteGastos(LimiteGastos limite) {
        LimiteGastosBackupDTO dto = new LimiteGastosBackupDTO();
        dto.setId(limite.getId());
        dto.setDescricao(limite.getDescricao());
        dto.setValor(limite.getValor());
        dto.setData(limite.getData());
        return dto;
    }
    
    private List<GraficoBackupDTO> converterGraficos() {
        return graficosRepository.findAll().stream()
                .map(this::converterGrafico)
                .collect(Collectors.toList());
    }
    
    private GraficoBackupDTO converterGrafico(Grafico grafico) {
        GraficoBackupDTO dto = new GraficoBackupDTO();
        dto.setId(grafico.getId());
        dto.setNome(grafico.getNome());
        dto.setTipo(grafico.getTipo());
        return dto;
    }
    
    private List<ExecucaoTarefaBackupDTO> converterExecucoesTarefas() {
        return execucaoTarefaRepository.findAll().stream()
                .map(this::converterExecucaoTarefa)
                .collect(Collectors.toList());
    }
    
    private ExecucaoTarefaBackupDTO converterExecucaoTarefa(ExecucaoTarefa execucao) {
        ExecucaoTarefaBackupDTO dto = new ExecucaoTarefaBackupDTO();
        dto.setId(execucao.getId());
        dto.setNomeTarefa(execucao.getNomeTarefa());
        dto.setDataExecucao(execucao.getDataExecucao());
        return dto;
    }
    
    private List<DespesaBackupDTO> converterDespesas() {
        return despesaRepository.findAll().stream()
                .map(this::converterDespesa)
                .collect(Collectors.toList());
    }
    
    private DespesaBackupDTO converterDespesa(Despesa despesa) {
        DespesaBackupDTO dto = new DespesaBackupDTO();
        dto.setId(despesa.getId());
        dto.setDescricao(despesa.getDescricao());
        dto.setValor(despesa.getValor());
        dto.setData(despesa.getData());
        dto.setContaId(despesa.getConta() != null ? despesa.getConta().getId() : null);
        dto.setCartaoId(despesa.getCartao() != null ? despesa.getCartao().getId() : null);
        dto.setCategoriaId(despesa.getCategoria() != null ? despesa.getCategoria().getId() : null);
        dto.setAnexo(despesa.getAnexo());
        dto.setObservacao(despesa.getObservacao());
        dto.setNumeroParcela(despesa.getNumeroParcela());
        dto.setTotalParcelas(despesa.getTotalParcelas());
        dto.setPago(despesa.getPago());
        return dto;
    }
    
    private List<ReceitaBackupDTO> converterReceitas() {
        return receitaRepository.findAll().stream()
                .map(this::converterReceita)
                .collect(Collectors.toList());
    }
    
    private ReceitaBackupDTO converterReceita(Receita receita) {
        ReceitaBackupDTO dto = new ReceitaBackupDTO();
        dto.setId(receita.getId());
        dto.setDescricao(receita.getDescricao());
        dto.setValor(receita.getValor());
        dto.setData(receita.getData());
        dto.setContaId(receita.getConta() != null ? receita.getConta().getId() : null);
        dto.setCategoriaId(receita.getCategoria() != null ? receita.getCategoria().getId() : null);
        dto.setAnexo(receita.getAnexo());
        dto.setObservacao(receita.getObservacao());
        return dto;
    }
    
    private List<NotificacaoBackupDTO> converterNotificacoes() {
        return notificacaoRepository.findAll().stream()
                .map(this::converterNotificacao)
                .collect(Collectors.toList());
    }
    
    private NotificacaoBackupDTO converterNotificacao(Notificacao notificacao) {
        NotificacaoBackupDTO dto = new NotificacaoBackupDTO();
        dto.setId(notificacao.getId());
        dto.setMensagem(notificacao.getMensagem());
        dto.setDataGeracao(notificacao.getDataGeracao());
        dto.setLida(notificacao.isLida());
        dto.setCartaoId(notificacao.getCartao() != null ? notificacao.getCartao().getId() : null);
        return dto;
    }
    
    private List<HistoricoBackupDTO> converterHistoricos() {
        return historicoRepository.findAll().stream()
                .map(this::converterHistorico)
                .collect(Collectors.toList());
    }
    
    private HistoricoBackupDTO converterHistorico(Historico historico) {
        HistoricoBackupDTO dto = new HistoricoBackupDTO();
        dto.setId(historico.getId());
        dto.setTipoOperacao(historico.getTipoOperacao() != null ? historico.getTipoOperacao().toString() : null);
        dto.setEntidade(historico.getEntidade());
        dto.setEntidadeId(historico.getEntidadeId());
        dto.setUsuarioId(historico.getUsuario() != null ? historico.getUsuario().getId() : null);
        dto.setInfo(historico.getInfo());
        dto.setDataHora(historico.getDataHora());
        return dto;
    }

    // Métodos de restauração com mapeamento
    
    private Map<Long, Long> restaurarUsuariosComMapeamento(List<UsuarioBackupDTO> usuarios) {
        Map<Long, Long> mapa = new HashMap<>();
        if (usuarios == null) return mapa;
        
        for (UsuarioBackupDTO dto : usuarios) {
            Usuario usuario = new Usuario();
            usuario.setNome(dto.getNome());
            usuario.setSenha(dto.getSenha());
            Usuario salvo = usuarioRepository.save(usuario);
            mapa.put(dto.getId(), salvo.getId());
        }
        return mapa;
    }
    
    private Map<Long, Long> restaurarCategoriasComMapeamento(List<CategoriaBackupDTO> categorias) {
        Map<Long, Long> mapa = new HashMap<>();
        if (categorias == null) return mapa;
        
        for (CategoriaBackupDTO dto : categorias) {
            Categoria categoria = new Categoria();
            categoria.setNome(dto.getNome());
            categoria.setCor(dto.getCor());
            categoria.setTipoReceita(dto.getTipoReceita());
            Categoria salva = categoriaRepository.save(categoria);
            mapa.put(dto.getId(), salva.getId());
        }
        return mapa;
    }
    
    private Map<Long, Long> restaurarContasComMapeamento(List<ContaBackupDTO> contas) {
        Map<Long, Long> mapa = new HashMap<>();
        if (contas == null) return mapa;
        
        for (ContaBackupDTO dto : contas) {
            Conta conta = new Conta();
            conta.setNome(dto.getNome());
            if (dto.getTipo() != null) {
                conta.setTipo(TipoConta.valueOf(dto.getTipo()));
            }
            conta.setImagemLogo(dto.getImagemLogo());
            Conta salva = contaRepository.save(conta);
            mapa.put(dto.getId(), salva.getId());
        }
        return mapa;
    }
    
    private Map<Long, Long> restaurarCartoesComMapeamento(List<CartaoBackupDTO> cartoes) {
        Map<Long, Long> mapa = new HashMap<>();
        if (cartoes == null) return mapa;
        
        System.out.println("Iniciando restauração de " + cartoes.size() + " cartões...");
        
        for (CartaoBackupDTO dto : cartoes) {
            Cartao cartao = new Cartao();
            cartao.setNome(dto.getNome());
            cartao.setLimite(dto.getLimite());
            cartao.setLimiteUsado(dto.getLimiteUsado());
            cartao.setDiaFechamento(dto.getDiaFechamento());
            cartao.setDiaVencimento(dto.getDiaVencimento());
            Cartao salvo = cartaoRepository.save(cartao);
            mapa.put(dto.getId(), salvo.getId());
            System.out.println("Cartão mapeado: " + dto.getId() + " -> " + salvo.getId() + " (" + salvo.getNome() + ")");
        }
        
        System.out.println("Mapa de cartões criado com " + mapa.size() + " entradas");
        return mapa;
    }

    // Métodos de restauração
    
    private void restaurarUsuarios(List<UsuarioBackupDTO> usuarios) {
        if (usuarios == null) return;
        
        for (UsuarioBackupDTO dto : usuarios) {
            Usuario usuario = new Usuario();
            usuario.setNome(dto.getNome());
            usuario.setSenha(dto.getSenha());
            usuarioRepository.save(usuario);
        }
    }
    
    private void restaurarCategorias(List<CategoriaBackupDTO> categorias) {
        if (categorias == null) return;
        
        for (CategoriaBackupDTO dto : categorias) {
            Categoria categoria = new Categoria();
            categoria.setNome(dto.getNome());
            categoria.setCor(dto.getCor());
            categoria.setTipoReceita(dto.getTipoReceita());
            categoriaRepository.save(categoria);
        }
    }
    
    private void restaurarContas(List<ContaBackupDTO> contas) {
        if (contas == null) return;
        
        for (ContaBackupDTO dto : contas) {
            Conta conta = new Conta();
            conta.setNome(dto.getNome());
            if (dto.getTipo() != null) {
                conta.setTipo(TipoConta.valueOf(dto.getTipo()));
            }
            conta.setImagemLogo(dto.getImagemLogo());
            contaRepository.save(conta);
        }
    }
    
    private void restaurarCartoes(List<CartaoBackupDTO> cartoes) {
        if (cartoes == null) return;
        
        for (CartaoBackupDTO dto : cartoes) {
            Cartao cartao = new Cartao();
            cartao.setNome(dto.getNome());
            cartao.setLimite(dto.getLimite());
            cartao.setLimiteUsado(dto.getLimiteUsado());
            cartao.setDiaFechamento(dto.getDiaFechamento());
            cartao.setDiaVencimento(dto.getDiaVencimento());
            cartaoRepository.save(cartao);
        }
    }
    
    private void restaurarPensamentos(List<PensamentosBackupDTO> pensamentos) {
        if (pensamentos == null) return;
        
        for (PensamentosBackupDTO dto : pensamentos) {
            Pensamentos pensamento = new Pensamentos();
            pensamento.setTexto(dto.getTexto());
            pensamentosRepository.save(pensamento);
        }
    }
    
    private void restaurarLimitesGastos(List<LimiteGastosBackupDTO> limites) {
        if (limites == null) return;
        
        for (LimiteGastosBackupDTO dto : limites) {
            LimiteGastos limite = new LimiteGastos();
            limite.setDescricao(dto.getDescricao());
            limite.setValor(dto.getValor());
            limite.setData(dto.getData());
            limiteGastosRepository.save(limite);
        }
    }
    
    private void restaurarGraficos(List<GraficoBackupDTO> graficos) {
        if (graficos == null) return;
        
        for (GraficoBackupDTO dto : graficos) {
            Grafico grafico = new Grafico();
            grafico.setNome(dto.getNome());
            grafico.setTipo(dto.getTipo());
            graficosRepository.save(grafico);
        }
    }
    
    private void restaurarExecucoesTarefas(List<ExecucaoTarefaBackupDTO> execucoes) {
        if (execucoes == null) return;
        
        for (ExecucaoTarefaBackupDTO dto : execucoes) {
            ExecucaoTarefa execucao = new ExecucaoTarefa();
            execucao.setNomeTarefa(dto.getNomeTarefa());
            execucao.setDataExecucao(dto.getDataExecucao());
            execucaoTarefaRepository.save(execucao);
        }
    }
    
    private void restaurarDespesasComMapeamento(List<DespesaBackupDTO> despesas, Map<Long, Long> mapaContas, Map<Long, Long> mapaCartoes, Map<Long, Long> mapaCategorias) {
        if (despesas == null) return;
        
        for (DespesaBackupDTO dto : despesas) {
            Despesa despesa = new Despesa();
            despesa.setDescricao(dto.getDescricao());
            despesa.setValor(dto.getValor());
            despesa.setData(dto.getData());
            
            // Resolver relacionamentos usando mapeamento de IDs
            if (dto.getContaId() != null && mapaContas.containsKey(dto.getContaId())) {
                Long novoIdConta = mapaContas.get(dto.getContaId());
                despesa.setConta(contaRepository.findById(novoIdConta).orElse(null));
            }
            if (dto.getCartaoId() != null && mapaCartoes.containsKey(dto.getCartaoId())) {
                Long novoIdCartao = mapaCartoes.get(dto.getCartaoId());
                despesa.setCartao(cartaoRepository.findById(novoIdCartao).orElse(null));
            }
            if (dto.getCategoriaId() != null && mapaCategorias.containsKey(dto.getCategoriaId())) {
                Long novoIdCategoria = mapaCategorias.get(dto.getCategoriaId());
                despesa.setCategoria(categoriaRepository.findById(novoIdCategoria).orElse(null));
            }
            
            despesa.setAnexo(dto.getAnexo());
            despesa.setObservacao(dto.getObservacao());
            despesa.setNumeroParcela(dto.getNumeroParcela());
            despesa.setTotalParcelas(dto.getTotalParcelas());
            despesa.setPago(dto.getPago());
            despesaRepository.save(despesa);
        }
    }
    
    private void restaurarReceitasComMapeamento(List<ReceitaBackupDTO> receitas, Map<Long, Long> mapaContas, Map<Long, Long> mapaCategorias) {
        if (receitas == null) return;
        
        for (ReceitaBackupDTO dto : receitas) {
            Receita receita = new Receita();
            receita.setDescricao(dto.getDescricao());
            receita.setValor(dto.getValor());
            receita.setData(dto.getData());
            
            // Resolver relacionamentos usando mapeamento de IDs
            if (dto.getContaId() != null && mapaContas.containsKey(dto.getContaId())) {
                Long novoIdConta = mapaContas.get(dto.getContaId());
                receita.setConta(contaRepository.findById(novoIdConta).orElse(null));
            }
            if (dto.getCategoriaId() != null && mapaCategorias.containsKey(dto.getCategoriaId())) {
                Long novoIdCategoria = mapaCategorias.get(dto.getCategoriaId());
                receita.setCategoria(categoriaRepository.findById(novoIdCategoria).orElse(null));
            }
            
            receita.setAnexo(dto.getAnexo());
            receita.setObservacao(dto.getObservacao());
            receitaRepository.save(receita);
        }
    }
    
    private void restaurarNotificacoesComMapeamento(List<NotificacaoBackupDTO> notificacoes, Map<Long, Long> mapaCartoes) {
        if (notificacoes == null) return;
        
        for (NotificacaoBackupDTO dto : notificacoes) {
            // Debug: verificar se o cartão original existe no mapa
            if (dto.getCartaoId() == null) {
                System.out.println("AVISO: Notificação sem cartão encontrada, pulando...");
                continue; // Pular notificações sem cartão
            }
            
            if (!mapaCartoes.containsKey(dto.getCartaoId())) {
                System.out.println("AVISO: Cartão ID " + dto.getCartaoId() + " não encontrado no mapa, pulando notificação...");
                continue; // Pular se cartão não foi mapeado
            }
            
            Long novoIdCartao = mapaCartoes.get(dto.getCartaoId());
            Cartao cartao = cartaoRepository.findById(novoIdCartao).orElse(null);
            
            if (cartao == null) {
                System.out.println("AVISO: Cartão com novo ID " + novoIdCartao + " não encontrado no banco, pulando notificação...");
                continue; // Pular se cartão não existe no banco
            }
            
            // Criar e salvar notificação apenas se cartão foi encontrado
            Notificacao notificacao = new Notificacao();
            notificacao.setMensagem(dto.getMensagem());
            notificacao.setDataGeracao(dto.getDataGeracao());
            notificacao.setLida(dto.getLida() != null ? dto.getLida() : false);
            notificacao.setCartao(cartao);
            
            try {
                notificacaoRepository.save(notificacao);
                System.out.println("Notificação salva com sucesso para cartão: " + cartao.getNome());
            } catch (Exception e) {
                System.out.println("ERRO ao salvar notificação: " + e.getMessage());
            }
        }
    }
    
    private void restaurarHistoricosComMapeamento(List<HistoricoBackupDTO> historicos, Map<Long, Long> mapaUsuarios) {
        if (historicos == null) return;
        
        for (HistoricoBackupDTO dto : historicos) {
            Historico historico = new Historico();
            if (dto.getTipoOperacao() != null) {
                historico.setTipoOperacao(Historico.TipoOperacao.valueOf(dto.getTipoOperacao()));
            }
            historico.setEntidade(dto.getEntidade());
            historico.setEntidadeId(dto.getEntidadeId());
            
            // Resolver relacionamento usando mapeamento de ID
            if (dto.getUsuarioId() != null && mapaUsuarios.containsKey(dto.getUsuarioId())) {
                Long novoIdUsuario = mapaUsuarios.get(dto.getUsuarioId());
                historico.setUsuario(usuarioRepository.findById(novoIdUsuario).orElse(null));
            }
            
            historico.setInfo(dto.getInfo());
            historico.setDataHora(dto.getDataHora());
            historicoRepository.save(historico);
        }
    }

    private Integer calcularTotalRegistros(BackupDTO backup) {
        int total = 0;
        
        if (backup.getUsuarios() != null) total += backup.getUsuarios().size();
        if (backup.getCategorias() != null) total += backup.getCategorias().size();
        if (backup.getContas() != null) total += backup.getContas().size();
        if (backup.getCartoes() != null) total += backup.getCartoes().size();
        if (backup.getPensamentos() != null) total += backup.getPensamentos().size();
        if (backup.getLimitesGastos() != null) total += backup.getLimitesGastos().size();
        if (backup.getGraficos() != null) total += backup.getGraficos().size();
        if (backup.getExecucoesTarefas() != null) total += backup.getExecucoesTarefas().size();
        if (backup.getDespesas() != null) total += backup.getDespesas().size();
        if (backup.getReceitas() != null) total += backup.getReceitas().size();
        if (backup.getNotificacoes() != null) total += backup.getNotificacoes().size();
        if (backup.getHistoricos() != null) total += backup.getHistoricos().size();
        
        return total;
    }
} 