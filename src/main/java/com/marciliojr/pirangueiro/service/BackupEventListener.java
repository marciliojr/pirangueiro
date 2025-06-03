package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.BackupDTO;
import com.marciliojr.pirangueiro.event.BackupImportFinalizadoEvent;
import com.marciliojr.pirangueiro.event.BackupImportIniciadoEvent;
import com.marciliojr.pirangueiro.model.StatusImportacao;
import com.marciliojr.pirangueiro.repository.StatusImportacaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Listener responsável por processar eventos de importação de backup
 */
@Component
@Slf4j
public class BackupEventListener {
    
    @Autowired
    private BackupService backupService;
    
    @Autowired
    private StatusImportacaoRepository statusRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${relatorio.email.destinatario}")
    private String emailDestinatario;
    
    @Value("${relatorio.email.enabled:true}")
    private boolean emailHabilitado;
    
    /**
     * Processa o evento de início de importação de forma assíncrona
     */
    @EventListener
    @Async
    @Transactional
    public void processarImportacao(BackupImportIniciadoEvent event) {
        String requestId = event.getRequestId();
        
        try {
            log.info("Iniciando processamento assíncrono de importação - RequestID: {}", requestId);
            
            // Criar registro de status
            StatusImportacao status = StatusImportacao.criar(requestId, event.getNomeArquivo());
            statusRepository.save(status);
            
            // Atualizar status para "PROCESSANDO"
            atualizarStatus(requestId, StatusImportacao.StatusEnum.PROCESSANDO, "Deserializando arquivo...");
            
            // Deserializar backup usando o conteúdo em bytes
            BackupDTO backup = backupService.deserializarBackup(event.getConteudoArquivo());
            
            // Validar estrutura
            if (backup.getDataGeracao() == null || backup.getVersao() == null) {
                throw new IllegalArgumentException("Arquivo de backup inválido - metadados ausentes");
            }
            
            atualizarStatus(requestId, StatusImportacao.StatusEnum.PROCESSANDO, "Validando dados...");
            
            // Atualizar informações do backup no status
            StatusImportacao statusAtualizado = statusRepository.findById(requestId).orElseThrow();
            statusAtualizado.setTotalRegistros(backup.getTotalRegistros());
            statusAtualizado.setVersaoBackup(backup.getVersao());
            statusRepository.save(statusAtualizado);
            
            atualizarStatus(requestId, StatusImportacao.StatusEnum.PROCESSANDO, "Restaurando dados no sistema...");
            
            // Executar restauração
            backupService.restaurarBackup(backup);
            
            // Atualizar status para sucesso
            atualizarStatus(requestId, StatusImportacao.StatusEnum.CONCLUIDO, 
                "Backup restaurado com sucesso! " + backup.getTotalRegistros() + " registros processados.");
            
            // Publicar evento de finalização
            eventPublisher.publishEvent(
                new BackupImportFinalizadoEvent(this, requestId, true, "Backup restaurado com sucesso!", backup)
            );
            
            log.info("Importação concluída com sucesso - RequestID: {}, Registros: {}", 
                requestId, backup.getTotalRegistros());
            
        } catch (Exception e) {
            log.error("Erro durante importação - RequestID: {}, Erro: {}", requestId, e.getMessage(), e);
            
            // Atualizar status para erro
            StatusImportacao statusErro = statusRepository.findById(requestId).orElse(null);
            if (statusErro != null) {
                statusErro.atualizar(StatusImportacao.StatusEnum.ERRO, "Erro: " + e.getMessage());
                statusErro.setDetalhesErro(e.getClass().getSimpleName() + ": " + e.getMessage());
                statusRepository.save(statusErro);
            }
            
            // Publicar evento de erro
            eventPublisher.publishEvent(
                new BackupImportFinalizadoEvent(this, requestId, false, e.getMessage(), null, e)
            );
        }
    }
    
    /**
     * Processa o evento de finalização de importação
     */
    @EventListener
    public void onImportacaoFinalizada(BackupImportFinalizadoEvent event) {
        log.info("Importação finalizada - RequestID: {}, Sucesso: {}", 
                event.getRequestId(), event.isSucesso());
        
        // Enviar email de notificação
        enviarEmailNotificacao(event);
        
        // Aqui pode adicionar outras ações pós-importação:
        // - Limpar arquivos temporários
        // - Registrar auditoria adicional
        // - Atualizar dashboards
        // - Enviar notificações para outros sistemas
    }
    
    /**
     * Atualiza o status da importação
     */
    private void atualizarStatus(String requestId, StatusImportacao.StatusEnum status, String mensagem) {
        try {
            StatusImportacao statusImportacao = statusRepository.findById(requestId).orElseThrow();
            statusImportacao.atualizar(status, mensagem);
            statusRepository.save(statusImportacao);
            log.debug("Status atualizado - RequestID: {}, Status: {}, Mensagem: {}", 
                requestId, status, mensagem);
        } catch (Exception e) {
            log.error("Erro ao atualizar status - RequestID: {}, Erro: {}", requestId, e.getMessage());
        }
    }
    
    /**
     * Envia email de notificação sobre o resultado da importação
     */
    private void enviarEmailNotificacao(BackupImportFinalizadoEvent event) {
        if (!emailHabilitado) {
            log.info("Email desabilitado - não enviando notificação de importação");
            return;
        }
        
        if (!emailService.isEmailConfigurado()) {
            log.warn("Email não configurado - não enviando notificação de importação");
            return;
        }
        
        try {
            log.info("Enviando email de notificação de importação - RequestID: {}", event.getRequestId());
            
            Context context = new Context();
            context.setVariable("requestId", event.getRequestId());
            context.setVariable("sucesso", event.isSucesso());
            context.setVariable("mensagem", event.getMensagem());
            context.setVariable("dataFinalizacao", event.getDataEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            String emoji = event.isSucesso() ? "✅" : "❌";
            String status = event.isSucesso() ? "SUCESSO" : "ERRO";
            
            if (event.isSucesso() && event.getBackup() != null) {
                context.setVariable("totalRegistros", event.getBackup().getTotalRegistros());
                context.setVariable("versaoBackup", event.getBackup().getVersao());
                context.setVariable("dataBackup", event.getBackup().getDataGeracao() != null ? 
                    event.getBackup().getDataGeracao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A");
            }
            
            if (!event.isSucesso() && event.getErro() != null) {
                context.setVariable("tipoErro", event.getErro().getClass().getSimpleName());
                context.setVariable("detalhesErro", event.getErro().getMessage());
            }
            
            String assunto = String.format("%s Importação de Backup %s - Sistema Pirangueiro", emoji, status);
            
            // Usar template HTML se disponível, senão usar texto simples
            try {
                emailService.enviarEmailHtml(emailDestinatario, assunto, "backup-import-notification", context);
                log.info("Email HTML de notificação enviado com sucesso - RequestID: {}", event.getRequestId());
            } catch (Exception e) {
                log.warn("Erro ao enviar email HTML, tentando texto simples: {}", e.getMessage());
                enviarEmailTextoSimples(event, assunto);
            }
            
        } catch (Exception e) {
            log.error("Erro ao enviar email de notificação - RequestID: {}, Erro: {}", 
                event.getRequestId(), e.getMessage());
        }
    }
    
    /**
     * Envia email de texto simples como fallback
     */
    private void enviarEmailTextoSimples(BackupImportFinalizadoEvent event, String assunto) {
        try {
            StringBuilder conteudo = new StringBuilder();
            conteudo.append("=== NOTIFICAÇÃO DE IMPORTAÇÃO DE BACKUP ===\n\n");
            
            conteudo.append("ID da Operação: ").append(event.getRequestId()).append("\n");
            conteudo.append("Status: ").append(event.isSucesso() ? "SUCESSO ✅" : "ERRO ❌").append("\n");
            conteudo.append("Data/Hora: ").append(event.getDataEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
            conteudo.append("Mensagem: ").append(event.getMensagem()).append("\n\n");
            
            if (event.isSucesso() && event.getBackup() != null) {
                conteudo.append("=== DETALHES DO BACKUP RESTAURADO ===\n");
                conteudo.append("Total de Registros: ").append(event.getBackup().getTotalRegistros()).append("\n");
                conteudo.append("Versão do Backup: ").append(event.getBackup().getVersao()).append("\n");
                if (event.getBackup().getDataGeracao() != null) {
                    conteudo.append("Data do Backup: ").append(
                        event.getBackup().getDataGeracao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    ).append("\n");
                }
            }
            
            if (!event.isSucesso() && event.getErro() != null) {
                conteudo.append("=== DETALHES DO ERRO ===\n");
                conteudo.append("Tipo do Erro: ").append(event.getErro().getClass().getSimpleName()).append("\n");
                conteudo.append("Detalhes: ").append(event.getErro().getMessage()).append("\n");
            }
            
            conteudo.append("\n---\nNotificação automática do Sistema Pirangueiro");
            
            emailService.enviarEmailTexto(emailDestinatario, assunto, conteudo.toString());
            log.info("Email de texto simples enviado com sucesso - RequestID: {}", event.getRequestId());
            
        } catch (Exception e) {
            log.error("Erro ao enviar email de texto simples - RequestID: {}, Erro: {}", 
                event.getRequestId(), e.getMessage());
        }
    }
} 