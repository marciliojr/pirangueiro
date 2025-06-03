package com.marciliojr.pirangueiro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela limpeza automática de registros antigos do sistema de backup
 */
@Service
@Slf4j
public class BackupCleanupService {
    
    @Autowired
    private BackupService backupService;
    
    /**
     * Executa limpeza automática de registros de status antigos
     * Executa todo domingo às 2h da manhã
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void executarLimpezaAutomatica() {
        try {
            log.info("Iniciando limpeza automática de registros de status de importação antigos...");
            
            backupService.limparStatusAntigos();
            
            log.info("Limpeza automática concluída com sucesso");
            
        } catch (Exception e) {
            log.error("Erro durante limpeza automática de registros antigos: {}", e.getMessage(), e);
        }
    }
} 