package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.RelatorioGerencialDTO;
import com.marciliojr.pirangueiro.service.RelatorioGerencialService;
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
        secaoReceitas.put("media", relatorio.getSecaoReceitas().getValorMedioReceitas());
        secaoReceitas.put("detalhes", relatorio.getSecaoReceitas().getTodasReceitas());
        relatorioEstruturado.put("secaoReceitas", secaoReceitas);

        // Seção Despesas (Cor Vermelha)
        Map<String, Object> secaoDespesas = new HashMap<>();
        secaoDespesas.put("titulo", "❤️ DESPESAS");
        secaoDespesas.put("cor", "vermelho");
        secaoDespesas.put("icone", "📉");
        secaoDespesas.put("total", relatorio.getSecaoDespesas().getTotalDespesas());
        secaoDespesas.put("quantidade", relatorio.getSecaoDespesas().getQuantidadeDespesas());
        secaoDespesas.put("media", relatorio.getSecaoDespesas().getValorMedioDespesas());
        secaoDespesas.put("detalhes", relatorio.getSecaoDespesas().getTodasDespesas());
        relatorioEstruturado.put("secaoDespesas", secaoDespesas);

        // Seção Saldos das Contas (Cor Azul)
        Map<String, Object> secaoContas = new HashMap<>();
        secaoContas.put("titulo", "🏦 SALDOS DAS CONTAS");
        secaoContas.put("cor", "azul");
        secaoContas.put("icone", "💳");
        secaoContas.put("saldoTotal", relatorio.getSecaoSaldosContas().getSaldoTotalContas());
        secaoContas.put("totalReceitas", relatorio.getSecaoSaldosContas().getTotalReceitasContas());
        secaoContas.put("totalDespesas", relatorio.getSecaoSaldosContas().getTotalDespesasContas());
        secaoContas.put("detalhes", relatorio.getSecaoSaldosContas().getSaldosDetalhados());
        relatorioEstruturado.put("secaoContas", secaoContas);

        // Seção Cartões (Cor Roxa)
        Map<String, Object> secaoCartoes = new HashMap<>();
        secaoCartoes.put("titulo", "💳 CARTÕES DE CRÉDITO");
        secaoCartoes.put("cor", "roxo");
        secaoCartoes.put("icone", "💳");
        secaoCartoes.put("limiteTotal", relatorio.getSecaoCartoes().getLimiteTotal());
        secaoCartoes.put("limiteUsado", relatorio.getSecaoCartoes().getLimiteUsadoTotal());
        secaoCartoes.put("limiteDisponivel", relatorio.getSecaoCartoes().getLimiteDisponivelTotal());
        secaoCartoes.put("percentualUtilizacao", relatorio.getSecaoCartoes().getPercentualUtilizacaoGeral());
        secaoCartoes.put("detalhes", relatorio.getSecaoCartoes().getCartoesDetalhados());
        relatorioEstruturado.put("secaoCartoes", secaoCartoes);

        // Seção Análise por Categoria (Cor Laranja)
        Map<String, Object> secaoCategoria = new HashMap<>();
        secaoCategoria.put("titulo", "📊 ANÁLISE POR CATEGORIA");
        secaoCategoria.put("cor", "laranja");
        secaoCategoria.put("icone", "📊");
        secaoCategoria.put("categoriaMaiorDespesa", relatorio.getSecaoAnaliseCategoria().getCategoriaMaiorDespesa());
        secaoCategoria.put("categoriaMaiorReceita", relatorio.getSecaoAnaliseCategoria().getCategoriaMaiorReceita());
        secaoCategoria.put("analiseCategoriaDespesas", relatorio.getSecaoAnaliseCategoria().getAnaliseCategoriaDespesas());
        secaoCategoria.put("analiseCategoriaReceitas", relatorio.getSecaoAnaliseCategoria().getAnaliseCategoriaReceitas());
        relatorioEstruturado.put("secaoCategoria", secaoCategoria);

        // Adicionar informações gerais
        relatorioEstruturado.put("dataGeracao", relatorio.getDataGeracao());
        relatorioEstruturado.put("versao", relatorio.getVersao());
        
        return relatorioEstruturado;
    }

    /**
     * Formata valor com status visual
     */
    private Map<String, Object> formatarValorComStatus(Double valor) {
        Map<String, Object> valorFormatado = new HashMap<>();
        valorFormatado.put("valor", valor);
        valorFormatado.put("formatado", String.format("R$ %.2f", valor));
        
        if (valor > 0) {
            valorFormatado.put("status", "positivo");
            valorFormatado.put("cor", "#28a745");
            valorFormatado.put("icone", "⬆️");
        } else if (valor < 0) {
            valorFormatado.put("status", "negativo");
            valorFormatado.put("cor", "#dc3545");
            valorFormatado.put("icone", "⬇️");
        } else {
            valorFormatado.put("status", "neutro");
            valorFormatado.put("cor", "#6c757d");
            valorFormatado.put("icone", "➖");
        }
        
        return valorFormatado;
    }

    /**
     * Formata situação financeira com visual correspondente
     */
    private Map<String, Object> formatarSituacaoFinanceira(String situacao) {
        Map<String, Object> situacaoFormatada = new HashMap<>();
        situacaoFormatada.put("texto", situacao);
        
        switch (situacao.toUpperCase()) {
            case "EXCELENTE":
                situacaoFormatada.put("cor", "#28a745");
                situacaoFormatada.put("icone", "🌟");
                situacaoFormatada.put("nivel", 5);
                break;
            case "BOA":
                situacaoFormatada.put("cor", "#20c997");
                situacaoFormatada.put("icone", "✅");
                situacaoFormatada.put("nivel", 4);
                break;
            case "REGULAR":
                situacaoFormatada.put("cor", "#ffc107");
                situacaoFormatada.put("icone", "⚠️");
                situacaoFormatada.put("nivel", 3);
                break;
            case "RUIM":
                situacaoFormatada.put("cor", "#fd7e14");
                situacaoFormatada.put("icone", "🚨");
                situacaoFormatada.put("nivel", 2);
                break;
            case "CRÍTICA":
                situacaoFormatada.put("cor", "#dc3545");
                situacaoFormatada.put("icone", "🚩");
                situacaoFormatada.put("nivel", 1);
                break;
            default:
                situacaoFormatada.put("cor", "#6c757d");
                situacaoFormatada.put("icone", "❓");
                situacaoFormatada.put("nivel", 0);
        }
        
        return situacaoFormatada;
    }

    /**
     * Extrai indicadores principais do relatório
     */
    private Map<String, Object> extrairIndicadoresPrincipais(RelatorioGerencialDTO relatorio) {
        Map<String, Object> indicadores = new HashMap<>();
        
        // Indicador de liquidez
        Double totalReceitas = relatorio.getSecaoReceitas().getTotalReceitas();
        Double totalDespesas = relatorio.getSecaoDespesas().getTotalDespesas();
        Double liquidez = totalDespesas > 0 ? (totalReceitas / totalDespesas) : 0.0;
        
        Map<String, Object> indicadorLiquidez = new HashMap<>();
        indicadorLiquidez.put("valor", liquidez);
        indicadorLiquidez.put("descricao", "Relação entre receitas e despesas");
        indicadorLiquidez.put("status", liquidez >= 1.2 ? "Excelente" : liquidez >= 1.0 ? "Bom" : "Atenção");
        indicadores.put("liquidez", indicadorLiquidez);
        
        // Indicador de utilização de cartão
        Double percentualCartao = relatorio.getSecaoCartoes().getPercentualUtilizacaoGeral();
        Map<String, Object> indicadorCartao = new HashMap<>();
        indicadorCartao.put("valor", percentualCartao);
        indicadorCartao.put("descricao", "Utilização do limite dos cartões");
        indicadorCartao.put("status", percentualCartao <= 30 ? "Excelente" : percentualCartao <= 50 ? "Bom" : "Atenção");
        indicadores.put("utilizacaoCartao", indicadorCartao);
        
        return indicadores;
    }
} 