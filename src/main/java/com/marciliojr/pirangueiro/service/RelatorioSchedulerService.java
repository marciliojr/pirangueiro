package com.marciliojr.pirangueiro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service responsável pelo agendamento automático de relatórios
 */
@Service
@Slf4j
public class RelatorioSchedulerService {

    @Autowired
    private RelatorioEmailService relatorioEmailService;

    @Value("${relatorio.email.enabled:true}")
    private boolean emailHabilitado;

    @Value("${relatorio.email.destinatario}")
    private String emailDestinatario;

    // Flag para controlar se já foi enviado hoje
    private final AtomicBoolean enviadoHoje = new AtomicBoolean(false);
    private LocalDate ultimoEnvio = null;

    /**
     * Execução na primeira inicialização do sistema
     * Verifica se precisa enviar o relatório do dia
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!emailHabilitado) {
            log.info("Sistema iniciado - Envio de relatório por email está desabilitado");
            return;
        }

        log.info("Sistema iniciado - Verificando necessidade de envio de relatório...");
        
        try {
            LocalDate hoje = LocalDate.now();
            
            // Se nunca foi enviado ou foi enviado em outro dia, envia
            if (ultimoEnvio == null || !ultimoEnvio.equals(hoje)) {
                log.info("Enviando relatório inicial do sistema para o dia: {}", hoje);
                enviarRelatorioDiario();
                marcarComoEnviadoHoje();
            } else {
                log.info("Relatório já foi enviado hoje: {}", hoje);
            }
            
        } catch (Exception e) {
            log.error("Erro ao enviar relatório inicial: {}", e.getMessage());
        }
    }

    /**
     * Execução agendada diariamente às 7h da manhã
     * Cron: "0 0 7 * * ?" = segundo minuto hora dia mês dia_da_semana
     */
    @Scheduled(cron = "${relatorio.email.horario:0 0 7 * * ?}")
    public void enviarRelatorioAgendado() {
        if (!emailHabilitado) {
            return;
        }

        try {
            LocalDate hoje = LocalDate.now();
            
            // Verifica se já foi enviado hoje
            if (ultimoEnvio != null && ultimoEnvio.equals(hoje) && enviadoHoje.get()) {
                log.debug("Relatório já foi enviado hoje: {}", hoje);
                return;
            }
            
            log.info("Executando envio automático de relatório às 07:00h - Data: {}", hoje);
            enviarRelatorioDiario();
            marcarComoEnviadoHoje();
            
        } catch (Exception e) {
            log.error("Erro no envio automático de relatório: {}", e.getMessage());
        }
    }

    /**
     * Método principal para envio do relatório diário
     */
    private void enviarRelatorioDiario() {
        try {
            log.info("Iniciando geração e envio do relatório diário...");
            
            // Tenta enviar email HTML primeiro
            try {
                relatorioEmailService.enviarRelatorioEmail();
                log.info("Relatório HTML enviado com sucesso");
            } catch (Exception e) {
                log.warn("Falha ao enviar relatório HTML, tentando texto simples: {}", e.getMessage());
                // Fallback para texto simples
                relatorioEmailService.enviarRelatorioTextoSimples(emailDestinatario);
                log.info("Relatório em texto simples enviado com sucesso");
            }
            
        } catch (Exception e) {
            log.error("Falha completa no envio do relatório: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Marca que o relatório foi enviado hoje
     */
    private void marcarComoEnviadoHoje() {
        ultimoEnvio = LocalDate.now();
        enviadoHoje.set(true);
        log.debug("Relatório marcado como enviado para o dia: {}", ultimoEnvio);
    }

    /**
     * Redefine o controle diário - executado à meia-noite
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void redefinirControleDiario() {
        enviadoHoje.set(false);
        log.debug("Controle diário redefinido às 00:00h");
    }

    /**
     * Força o envio manual do relatório (para testes ou uso manual)
     */
    public void forcarEnvioRelatorio() {
        forcarEnvioRelatorio(emailDestinatario);
    }

    /**
     * Força o envio manual do relatório para um destinatário específico
     */
    public void forcarEnvioRelatorio(String destinatario) {
        try {
            log.info("Forçando envio manual de relatório para: {}", destinatario);
            relatorioEmailService.enviarRelatorioEmail(destinatario);
            log.info("Envio manual concluído com sucesso");
        } catch (Exception e) {
            log.error("Erro no envio manual de relatório: {}", e.getMessage());
            throw new RuntimeException("Falha no envio manual", e);
        }
    }

    /**
     * Verifica o status do agendamento
     */
    public String obterStatusAgendamento() {
        StringBuilder status = new StringBuilder();
        status.append("Status do Agendamento de Relatórios:\n");
        status.append("- Email habilitado: ").append(emailHabilitado).append("\n");
        status.append("- Destinatário: ").append(emailDestinatario).append("\n");
        status.append("- Último envio: ").append(ultimoEnvio != null ? ultimoEnvio.toString() : "Nunca").append("\n");
        status.append("- Enviado hoje: ").append(enviadoHoje.get()).append("\n");
        status.append("- Data/hora atual: ").append(LocalDateTime.now()).append("\n");
        
        return status.toString();
    }

    /**
     * Redefine o status de envio (para testes)
     */
    public void redefinirStatusEnvio() {
        enviadoHoje.set(false);
        ultimoEnvio = null;
        log.info("Status de envio redefinido manualmente");
    }
} 