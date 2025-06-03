package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.RelatorioGerencialDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsável por gerar e enviar relatórios gerenciais por email
 */
@Service
@Slf4j
public class RelatorioEmailService {

    @Autowired
    private RelatorioGerencialService relatorioGerencialService;

    @Autowired
    private EmailService emailService;

    @Value("${relatorio.email.destinatario}")
    private String emailDestinatario;

    @Value("${relatorio.email.enabled:true}")
    private boolean emailHabilitado;

    /**
     * Gera resumo do relatório gerencial para email
     */
    public Map<String, Object> gerarResumoRelatorioEmail() {
        try {
            log.info("Gerando resumo do relatório para email...");
            
            RelatorioGerencialDTO relatorio = relatorioGerencialService.gerarRelatorioCompleto();
            
            Map<String, Object> resumo = new HashMap<>();
            
            // Dados do cabeçalho
            resumo.put("dataGeracao", LocalDateTime.now());
            resumo.put("titulo", "Relatório Financeiro Diário");
            
            // Resumo Executivo
            Map<String, Object> resumoExec = new HashMap<>();
            resumoExec.put("saldoGeral", relatorio.getResumoExecutivo().getSaldoGeral());
            resumoExec.put("situacaoFinanceira", relatorio.getResumoExecutivo().getSituacaoFinanceira());
            resumoExec.put("percentualEconomia", relatorio.getResumoExecutivo().getPercentualEconomia());
            resumoExec.put("recomendacoes", relatorio.getResumoExecutivo().getRecomendacoes());
            resumo.put("resumoExecutivo", resumoExec);
            
            // Totais principais
            Map<String, Object> totais = new HashMap<>();
            totais.put("totalReceitas", relatorio.getSecaoReceitas().getTotalReceitas());
            totais.put("totalDespesas", relatorio.getSecaoDespesas().getTotalDespesas());
            totais.put("qtdReceitas", relatorio.getSecaoReceitas().getQuantidadeReceitas());
            totais.put("qtdDespesas", relatorio.getSecaoDespesas().getQuantidadeDespesas());
            resumo.put("totais", totais);
            
            // Informações das contas
            Map<String, Object> contas = new HashMap<>();
            contas.put("saldoTotal", relatorio.getSecaoSaldosContas().getSaldoTotalContas());
            contas.put("totalContas", relatorio.getSecaoSaldosContas().getSaldosDetalhados().size());
            resumo.put("contas", contas);
            
            // Informações dos cartões
            Map<String, Object> cartoes = new HashMap<>();
            cartoes.put("limiteTotal", relatorio.getSecaoCartoes().getLimiteTotal());
            cartoes.put("limiteUsado", relatorio.getSecaoCartoes().getLimiteUsadoTotal());
            cartoes.put("percentualUtilizacao", relatorio.getSecaoCartoes().getPercentualUtilizacaoGeral());
            cartoes.put("totalCartoes", relatorio.getSecaoCartoes().getCartoesDetalhados().size());
            resumo.put("cartoes", cartoes);
            
            // Categoria com maior despesa
            if (relatorio.getSecaoAnaliseCategoria().getCategoriaMaiorDespesa() != null) {
                Map<String, Object> categoriaMaior = new HashMap<>();
                var catMaior = relatorio.getSecaoAnaliseCategoria().getCategoriaMaiorDespesa();
                categoriaMaior.put("nome", catMaior.getNomeCategoria());
                categoriaMaior.put("valor", catMaior.getValor());
                categoriaMaior.put("percentual", catMaior.getPercentual());
                resumo.put("categoriaMaiorDespesa", categoriaMaior);
            }
            
            log.info("Resumo do relatório gerado com sucesso");
            return resumo;
            
        } catch (Exception e) {
            log.error("Erro ao gerar resumo do relatório para email: {}", e.getMessage());
            throw new RuntimeException("Falha ao gerar resumo do relatório", e);
        }
    }

    /**
     * Envia relatório por email
     */
    public void enviarRelatorioEmail() {
        enviarRelatorioEmail(emailDestinatario);
    }

    /**
     * Envia relatório por email para destinatário específico
     */
    public void enviarRelatorioEmail(String destinatario) {
        if (!emailHabilitado) {
            log.info("Envio de email de relatório está desabilitado");
            return;
        }

        if (!emailService.isEmailConfigurado()) {
            log.warn("Configuração de email não está válida. Pulando envio do relatório.");
            return;
        }

        try {
            log.info("Iniciando envio de relatório por email para: {}", destinatario);
            
            Map<String, Object> dadosRelatorio = gerarResumoRelatorioEmail();
            
            Context context = new Context();
            context.setVariables(dadosRelatorio);
            
            String assunto = String.format("📊 Relatório Financeiro Diário - %s", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            emailService.enviarEmailHtml(destinatario, assunto, "relatorio-email", context);
            
            log.info("Relatório enviado por email com sucesso para: {}", destinatario);
            
        } catch (Exception e) {
            log.error("Erro ao enviar relatório por email: {}", e.getMessage());
            e.printStackTrace();
            // Não relança a exceção para não quebrar a aplicação
        }
    }

    /**
     * Envia relatório de texto simples (fallback)
     */
    public void enviarRelatorioTextoSimples(String destinatario) {
        if (!emailHabilitado) {
            log.info("Envio de email de relatório está desabilitado");
            return;
        }

        try {
            Map<String, Object> dados = gerarResumoRelatorioEmail();
            
            StringBuilder conteudo = new StringBuilder();
            conteudo.append("=== RELATÓRIO FINANCEIRO DIÁRIO ===\n\n");
            conteudo.append("Data: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
            
            // Resumo Executivo
            @SuppressWarnings("unchecked")
            Map<String, Object> resumoExec = (Map<String, Object>) dados.get("resumoExecutivo");
            conteudo.append("💼 RESUMO EXECUTIVO:\n");
            conteudo.append("Saldo Geral: R$ ").append(String.format("%.2f", resumoExec.get("saldoGeral"))).append("\n");
            conteudo.append("Situação: ").append(resumoExec.get("situacaoFinanceira")).append("\n");
            conteudo.append("Economia: ").append(String.format("%.1f%%", resumoExec.get("percentualEconomia"))).append("\n");
            conteudo.append("Recomendação: ").append(resumoExec.get("recomendacoes")).append("\n\n");
            
            // Totais
            @SuppressWarnings("unchecked")
            Map<String, Object> totais = (Map<String, Object>) dados.get("totais");
            conteudo.append("💰 TOTAIS:\n");
            conteudo.append("Receitas: R$ ").append(String.format("%.2f", totais.get("totalReceitas"))).append(" (").append(totais.get("qtdReceitas")).append(" lançamentos)\n");
            conteudo.append("Despesas: R$ ").append(String.format("%.2f", totais.get("totalDespesas"))).append(" (").append(totais.get("qtdDespesas")).append(" lançamentos)\n\n");
            
            // Contas
            @SuppressWarnings("unchecked")
            Map<String, Object> contas = (Map<String, Object>) dados.get("contas");
            conteudo.append("🏦 CONTAS (").append(contas.get("totalContas")).append(" contas):\n");
            conteudo.append("Saldo Total: R$ ").append(String.format("%.2f", contas.get("saldoTotal"))).append("\n\n");
            
            // Cartões
            @SuppressWarnings("unchecked")
            Map<String, Object> cartoes = (Map<String, Object>) dados.get("cartoes");
            conteudo.append("💳 CARTÕES (").append(cartoes.get("totalCartoes")).append(" cartões):\n");
            conteudo.append("Limite Total: R$ ").append(String.format("%.2f", cartoes.get("limiteTotal"))).append("\n");
            conteudo.append("Utilização: ").append(String.format("%.1f%%", cartoes.get("percentualUtilizacao"))).append("\n\n");
            
            conteudo.append("---\nRelatório gerado automaticamente pelo Sistema Pirangueiro");
            
            String assunto = String.format("📊 Relatório Financeiro Diário - %s", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            emailService.enviarEmailTexto(destinatario, assunto, conteudo.toString());
            
        } catch (Exception e) {
            log.error("Erro ao enviar relatório de texto simples: {}", e.getMessage());
        }
    }
} 