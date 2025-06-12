package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.RelatorioGerencialDTO;
import com.marciliojr.pirangueiro.service.RelatorioGerencialService;
import com.marciliojr.pirangueiro.service.RelatorioSchedulerService;
import com.marciliojr.pirangueiro.service.RelatorioEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsável por gerar relatórios gerenciais completos das finanças
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioGerencialService relatorioGerencialService;

    @Autowired
    private RelatorioSchedulerService relatorioSchedulerService;

    @Autowired
    private RelatorioEmailService relatorioEmailService;

    /**
     * Gera relatório gerencial completo das finanças
     * @param mes Mês para filtrar (opcional, de 1 a 12)
     * @param ano Ano para filtrar (opcional, ex: 2024)
     * @return Relatório detalhado com todas as informações financeiras organizadas
     */
    @GetMapping("/gerencial")
    public ResponseEntity<Map<String, Object>> gerarRelatorioGerencial(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        try {
            // Validar parâmetros
            if (mes != null && (mes < 1 || mes > 12)) {
                Map<String, Object> erro = new HashMap<>();
                erro.put("erro", "Mês inválido. Deve estar entre 1 e 12");
                erro.put("timestamp", LocalDateTime.now());
                return ResponseEntity.badRequest().body(erro);
            }
            
            if (ano != null && (ano < 1900 || ano > 2100)) {
                Map<String, Object> erro = new HashMap<>();
                erro.put("erro", "Ano inválido. Deve estar entre 1900 e 2100");
                erro.put("timestamp", LocalDateTime.now());
                return ResponseEntity.badRequest().body(erro);
            }

            RelatorioGerencialDTO relatorio = relatorioGerencialService.gerarRelatorioCompleto(mes, ano);
            
            // Estrutura organizada do relatório para o frontend
            Map<String, Object> relatorioFormatado = estruturarRelatorioParaFrontend(relatorio);
            
            // Adicionar informações do período filtrado
            Map<String, Object> filtro = new HashMap<>();
            if (mes != null && ano != null) {
                filtro.put("periodo", "Mês " + mes + "/" + ano);
                filtro.put("tipo", "MENSAL");
            } else if (ano != null) {
                filtro.put("periodo", "Ano " + ano);
                filtro.put("tipo", "ANUAL");
            } else {
                filtro.put("periodo", "Todos os registros");
                filtro.put("tipo", "COMPLETO");
            }
            filtro.put("mes", mes);
            filtro.put("ano", ano);
            relatorioFormatado.put("filtro", filtro);
            
            return ResponseEntity.ok(relatorioFormatado);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Falha ao gerar relatório gerencial");
            erro.put("detalhes", e.getMessage());
            erro.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Gera relatório gerencial em formato JSON para exportação
     * @param mes Mês para filtrar (opcional, de 1 a 12)
     * @param ano Ano para filtrar (opcional, ex: 2024)
     * @return Arquivo JSON com o relatório completo
     */
    @GetMapping("/gerencial/export/json")
    public ResponseEntity<RelatorioGerencialDTO> exportarRelatorioJson(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        try {
            // Validar parâmetros
            if (mes != null && (mes < 1 || mes > 12)) {
                return ResponseEntity.badRequest().build();
            }
            
            if (ano != null && (ano < 1900 || ano > 2100)) {
                return ResponseEntity.badRequest().build();
            }

            RelatorioGerencialDTO relatorio = relatorioGerencialService.gerarRelatorioCompleto(mes, ano);
            
            String nomeArquivo = "relatorio_gerencial";
            if (mes != null && ano != null) {
                nomeArquivo += "_" + String.format("%02d_%04d", mes, ano);
            } else if (ano != null) {
                nomeArquivo += "_" + ano;
            }
            nomeArquivo += "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", nomeArquivo);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(relatorio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gera relatório resumido com apenas indicadores principais
     * @param mes Mês para filtrar (opcional, de 1 a 12)
     * @param ano Ano para filtrar (opcional, ex: 2024)
     * @return Resumo executivo do relatório
     */
    @GetMapping("/gerencial/resumo")
    public ResponseEntity<Map<String, Object>> gerarResumoExecutivo(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        try {
            // Validar parâmetros
            if (mes != null && (mes < 1 || mes > 12)) {
                Map<String, Object> erro = new HashMap<>();
                erro.put("erro", "Mês inválido. Deve estar entre 1 e 12");
                erro.put("timestamp", LocalDateTime.now());
                return ResponseEntity.badRequest().body(erro);
            }
            
            if (ano != null && (ano < 1900 || ano > 2100)) {
                Map<String, Object> erro = new HashMap<>();
                erro.put("erro", "Ano inválido. Deve estar entre 1900 e 2100");
                erro.put("timestamp", LocalDateTime.now());
                return ResponseEntity.badRequest().body(erro);
            }

            RelatorioGerencialDTO relatorio = relatorioGerencialService.gerarRelatorioCompleto(mes, ano);
            
            Map<String, Object> resumo = new HashMap<>();
            resumo.put("timestamp", LocalDateTime.now());
            
            // Adicionar informações do período filtrado
            Map<String, Object> filtro = new HashMap<>();
            if (mes != null && ano != null) {
                filtro.put("periodo", "Mês " + mes + "/" + ano);
                filtro.put("tipo", "MENSAL");
            } else if (ano != null) {
                filtro.put("periodo", "Ano " + ano);
                filtro.put("tipo", "ANUAL");
            } else {
                filtro.put("periodo", "Todos os registros");
                filtro.put("tipo", "COMPLETO");
            }
            filtro.put("mes", mes);
            filtro.put("ano", ano);
            resumo.put("filtro", filtro);
            
            resumo.put("resumoExecutivo", relatorio.getResumoExecutivo());
            resumo.put("indicadoresPrincipais", extrairIndicadoresPrincipais(relatorio));
            
            return ResponseEntity.ok(resumo);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Falha ao gerar resumo executivo");
            erro.put("detalhes", e.getMessage());
            erro.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Gera relatório para email (mesmo formato usado no cron)
     * @return Dados formatados para email
     */
    @GetMapping("/gerencial/email")
    public ResponseEntity<Map<String, Object>> gerarRelatorioEmail() {
        try {
            Map<String, Object> dadosEmail = relatorioEmailService.gerarResumoRelatorioEmail();
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", true);
            resposta.put("dados", dadosEmail);
            resposta.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Falha ao gerar relatório para email");
            erro.put("detalhes", e.getMessage());
            erro.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Força envio manual de relatório por email
     * @return Status do envio
     */
    @PostMapping("/gerencial/enviar-email")
    public ResponseEntity<Map<String, Object>> enviarRelatorioEmail(@RequestParam(required = false) String email) {
        try {
            if (email != null && !email.trim().isEmpty()) {
                relatorioSchedulerService.forcarEnvioRelatorio(email.trim());
            } else {
                relatorioSchedulerService.forcarEnvioRelatorio();
            }
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", true);
            resposta.put("mensagem", "Relatório enviado por email com sucesso");
            resposta.put("destinatario", email != null ? email.trim() : "email padrão configurado");
            resposta.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("sucesso", false);
            erro.put("erro", "Falha ao enviar relatório por email");
            erro.put("detalhes", e.getMessage());
            erro.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Obtém status do agendamento de emails
     * @return Status detalhado do sistema de agendamento
     */
    @GetMapping("/agendamento/status")
    public ResponseEntity<Map<String, Object>> obterStatusAgendamento() {
        try {
            String statusTexto = relatorioSchedulerService.obterStatusAgendamento();
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("status", statusTexto);
            resposta.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Falha ao obter status do agendamento");
            erro.put("detalhes", e.getMessage());
            erro.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Redefine status de envio (para testes)
     * @return Confirmação da redefinição
     */
    @PostMapping("/agendamento/redefinir")
    public ResponseEntity<Map<String, Object>> redefinirStatusAgendamento() {
        try {
            relatorioSchedulerService.redefinirStatusEnvio();
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", true);
            resposta.put("mensagem", "Status de agendamento redefinido com sucesso");
            resposta.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("sucesso", false);
            erro.put("erro", "Falha ao redefinir status do agendamento");
            erro.put("detalhes", e.getMessage());
            erro.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Estrutura o relatório de forma organizada e visualmente atrativa para o frontend
     */
    private Map<String, Object> estruturarRelatorioParaFrontend(RelatorioGerencialDTO relatorio) {
        Map<String, Object> relatorioEstruturado = new HashMap<>();
        
        // Cabeçalho do relatório
        Map<String, Object> cabecalho = new HashMap<>();
        cabecalho.put("titulo", "📊 RELATÓRIO GERENCIAL FINANCEIRO");
        cabecalho.put("subtitulo", "Análise Completa das Finanças Pessoais");
        cabecalho.put("dataGeracao", relatorio.getDataGeracao());
        cabecalho.put("versao", relatorio.getVersao());
        cabecalho.put("icone", "💰");
        relatorioEstruturado.put("cabecalho", cabecalho);

        // Resumo Executivo (Destaque principal)
        Map<String, Object> resumoFormatado = new HashMap<>();
        RelatorioGerencialDTO.ResumoExecutivo resumo = relatorio.getResumoExecutivo();
        
        resumoFormatado.put("titulo", "📈 RESUMO EXECUTIVO");
        resumoFormatado.put("saldoGeral", formatarValorComStatus(resumo.getSaldoGeral()));
        resumoFormatado.put("situacaoFinanceira", formatarSituacaoFinanceira(resumo.getSituacaoFinanceira()));
        resumoFormatado.put("percentualEconomia", resumo.getPercentualEconomia());
        resumoFormatado.put("recomendacoes", resumo.getRecomendacoes());
        resumoFormatado.put("estilo", "destaque-principal");
        relatorioEstruturado.put("resumoExecutivo", resumoFormatado);

        // Seção Receitas (Cor Verde)
        Map<String, Object> secaoReceitas = new HashMap<>();
        secaoReceitas.put("titulo", "💚 RECEITAS");
        secaoReceitas.put("cor", "verde");
        secaoReceitas.put("icone", "📈");
        secaoReceitas.put("total", relatorio.getSecaoReceitas().getTotalReceitas());
        secaoReceitas.put("quantidade", relatorio.getSecaoReceitas().getQuantidadeReceitas());
        secaoReceitas.put("valorMedio", relatorio.getSecaoReceitas().getValorMedioReceitas());
        secaoReceitas.put("detalhes", relatorio.getSecaoReceitas().getTodasReceitas());
        secaoReceitas.put("estilo", "secao-positiva");
        relatorioEstruturado.put("receitas", secaoReceitas);

        // Seção Despesas (Cor Vermelha)
        Map<String, Object> secaoDespesas = new HashMap<>();
        secaoDespesas.put("titulo", "❤️ DESPESAS");
        secaoDespesas.put("cor", "vermelho");
        secaoDespesas.put("icone", "📉");
        secaoDespesas.put("total", relatorio.getSecaoDespesas().getTotalDespesas());
        secaoDespesas.put("quantidade", relatorio.getSecaoDespesas().getQuantidadeDespesas());
        secaoDespesas.put("valorMedio", relatorio.getSecaoDespesas().getValorMedioDespesas());
        secaoDespesas.put("detalhes", relatorio.getSecaoDespesas().getTodasDespesas());
        secaoDespesas.put("estilo", "secao-negativa");
        relatorioEstruturado.put("despesas", secaoDespesas);

        // Seção Saldos das Contas (Cor Azul)
        Map<String, Object> secaoContas = new HashMap<>();
        secaoContas.put("titulo", "💙 SALDOS DAS CONTAS");
        secaoContas.put("cor", "azul");
        secaoContas.put("icone", "🏦");
        secaoContas.put("saldoTotal", relatorio.getSecaoSaldosContas().getSaldoTotalContas());
        secaoContas.put("totalReceitas", relatorio.getSecaoSaldosContas().getTotalReceitasContas());
        secaoContas.put("totalDespesas", relatorio.getSecaoSaldosContas().getTotalDespesasContas());
        secaoContas.put("detalhesContas", formatarSaldosContas(relatorio.getSecaoSaldosContas().getSaldosDetalhados()));
        secaoContas.put("estilo", "secao-neutra");
        relatorioEstruturado.put("contasBancarias", secaoContas);

        // Seção Cartões de Crédito (Cor Laranja)
        Map<String, Object> secaoCartoes = new HashMap<>();
        secaoCartoes.put("titulo", "🧡 CARTÕES DE CRÉDITO");
        secaoCartoes.put("cor", "laranja");
        secaoCartoes.put("icone", "💳");
        secaoCartoes.put("limiteTotal", relatorio.getSecaoCartoes().getLimiteTotal());
        secaoCartoes.put("limiteUsado", relatorio.getSecaoCartoes().getLimiteUsadoTotal());
        secaoCartoes.put("limiteDisponivel", relatorio.getSecaoCartoes().getLimiteDisponivelTotal());
        secaoCartoes.put("percentualUtilizacao", relatorio.getSecaoCartoes().getPercentualUtilizacaoGeral());
        secaoCartoes.put("detalhesCartoes", formatarCartoesDetalhados(relatorio.getSecaoCartoes().getCartoesDetalhados()));
        secaoCartoes.put("estilo", "secao-cartoes");
        relatorioEstruturado.put("cartoesCredito", secaoCartoes);

        // Seção Análise por Categorias (Cor Roxa)
        Map<String, Object> secaoCategorias = new HashMap<>();
        secaoCategorias.put("titulo", "💜 ANÁLISE POR CATEGORIAS");
        secaoCategorias.put("cor", "roxo");
        secaoCategorias.put("icone", "📊");
        secaoCategorias.put("categoriaDespesas", relatorio.getSecaoAnaliseCategoria().getAnaliseCategoriaDespesas());
        secaoCategorias.put("categoriaReceitas", relatorio.getSecaoAnaliseCategoria().getAnaliseCategoriaReceitas());
        secaoCategorias.put("categoriaMaiorDespesa", relatorio.getSecaoAnaliseCategoria().getCategoriaMaiorDespesa());
        secaoCategorias.put("categoriaMaiorReceita", relatorio.getSecaoAnaliseCategoria().getCategoriaMaiorReceita());
        secaoCategorias.put("estilo", "secao-analise");
        relatorioEstruturado.put("analiseCategoria", secaoCategorias);

        // Indicadores Chave
        relatorioEstruturado.put("indicadoresChave", resumo.getIndicadoresChave());
        
        // Metadados para formatação no frontend
        Map<String, Object> metadados = new HashMap<>();
        metadados.put("totalSecoes", 6);
        metadados.put("formatoSugerido", "dashboard");
        metadados.put("exportacaoDisponivel", true);
        metadados.put("estilosTema", obterEstilosTemasCSS());
        relatorioEstruturado.put("metadados", metadados);

        return relatorioEstruturado;
    }

    private Map<String, Object> formatarValorComStatus(Double valor) {
        Map<String, Object> valorFormatado = new HashMap<>();
        valorFormatado.put("valor", valor);
        valorFormatado.put("formatado", String.format("R$ %.2f", valor));
        
        if (valor > 0) {
            valorFormatado.put("status", "POSITIVO");
            valorFormatado.put("cor", "#28a745");
            valorFormatado.put("icone", "📈");
        } else if (valor < 0) {
            valorFormatado.put("status", "NEGATIVO");
            valorFormatado.put("cor", "#dc3545");
            valorFormatado.put("icone", "📉");
        } else {
            valorFormatado.put("status", "NEUTRO");
            valorFormatado.put("cor", "#6c757d");
            valorFormatado.put("icone", "⚖️");
        }
        
        return valorFormatado;
    }

    private Map<String, Object> formatarSituacaoFinanceira(String situacao) {
        Map<String, Object> situacaoFormatada = new HashMap<>();
        situacaoFormatada.put("situacao", situacao);
        
        switch (situacao) {
            case "SAUDAVEL":
                situacaoFormatada.put("cor", "#28a745");
                situacaoFormatada.put("icone", "💚");
                situacaoFormatada.put("mensagem", "Excelente! Situação financeira saudável.");
                break;
            case "ATENCAO":
                situacaoFormatada.put("cor", "#ffc107");
                situacaoFormatada.put("icone", "⚠️");
                situacaoFormatada.put("mensagem", "Atenção! Há espaço para melhorias.");
                break;
            case "CRITICA":
                situacaoFormatada.put("cor", "#dc3545");
                situacaoFormatada.put("icone", "🚨");
                situacaoFormatada.put("mensagem", "Crítico! Revisão urgente necessária.");
                break;
        }
        
        return situacaoFormatada;
    }

    private Object formatarSaldosContas(java.util.List<RelatorioGerencialDTO.SaldoContaDetalhado> saldosDetalhados) {
        return saldosDetalhados.stream().map(saldo -> {
            Map<String, Object> contaFormatada = new HashMap<>();
            contaFormatada.put("nome", saldo.getNomeConta());
            contaFormatada.put("tipo", saldo.getTipoConta());
            contaFormatada.put("saldo", formatarValorComStatus(saldo.getSaldo()));
            contaFormatada.put("receitas", saldo.getTotalReceitas());
            contaFormatada.put("despesas", saldo.getTotalDespesas());
            contaFormatada.put("status", saldo.getStatusSaldo());
            return contaFormatada;
        }).collect(java.util.stream.Collectors.toList());
    }

    private Object formatarCartoesDetalhados(java.util.List<RelatorioGerencialDTO.CartaoDetalhado> cartoesDetalhados) {
        return cartoesDetalhados.stream().map(cartao -> {
            Map<String, Object> cartaoFormatado = new HashMap<>();
            cartaoFormatado.put("nome", cartao.getNomeCartao());
            cartaoFormatado.put("limite", cartao.getLimite());
            cartaoFormatado.put("usado", cartao.getLimiteUsado());
            cartaoFormatado.put("disponivel", cartao.getLimiteDisponivel());
            cartaoFormatado.put("percentualUsado", cartao.getPercentualUtilizacao());
            cartaoFormatado.put("statusUtilizacao", formatarStatusUtilizacao(cartao.getStatusUtilizacao()));
            cartaoFormatado.put("despesasPendentes", cartao.getDespesasNaoPagas().size());
            return cartaoFormatado;
        }).collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> formatarStatusUtilizacao(String status) {
        Map<String, Object> statusFormatado = new HashMap<>();
        statusFormatado.put("status", status);
        
        switch (status) {
            case "BAIXA":
                statusFormatado.put("cor", "#28a745");
                statusFormatado.put("icone", "🟢");
                break;
            case "MEDIA":
                statusFormatado.put("cor", "#ffc107");
                statusFormatado.put("icone", "🟡");
                break;
            case "ALTA":
                statusFormatado.put("cor", "#fd7e14");
                statusFormatado.put("icone", "🟠");
                break;
            case "CRITICA":
                statusFormatado.put("cor", "#dc3545");
                statusFormatado.put("icone", "🔴");
                break;
        }
        
        return statusFormatado;
    }

    private Map<String, Object> extrairIndicadoresPrincipais(RelatorioGerencialDTO relatorio) {
        Map<String, Object> indicadores = new HashMap<>();
        indicadores.put("totalReceitas", relatorio.getSecaoReceitas().getTotalReceitas());
        indicadores.put("totalDespesas", relatorio.getSecaoDespesas().getTotalDespesas());
        indicadores.put("saldoGeral", relatorio.getResumoExecutivo().getSaldoGeral());
        indicadores.put("percentualEconomia", relatorio.getResumoExecutivo().getPercentualEconomia());
        indicadores.put("situacaoFinanceira", relatorio.getResumoExecutivo().getSituacaoFinanceira());
        indicadores.put("utilizacaoCartoes", relatorio.getSecaoCartoes().getPercentualUtilizacaoGeral());
        return indicadores;
    }

    private Map<String, String> obterEstilosTemasCSS() {
        Map<String, String> estilos = new HashMap<>();
        estilos.put("corVerde", "#28a745");      // Receitas
        estilos.put("corVermelho", "#dc3545");   // Despesas
        estilos.put("corAzul", "#007bff");       // Contas
        estilos.put("corLaranja", "#fd7e14");    // Cartões
        estilos.put("corRoxo", "#6f42c1");       // Categorias
        estilos.put("corCinza", "#6c757d");      // Neutro
        estilos.put("fundoClaro", "#f8f9fa");
        estilos.put("fundoEscuro", "#343a40");
        estilos.put("bordaArredondada", "8px");
        estilos.put("sombra", "0 2px 4px rgba(0,0,0,0.1)");
        return estilos;
    }
} 