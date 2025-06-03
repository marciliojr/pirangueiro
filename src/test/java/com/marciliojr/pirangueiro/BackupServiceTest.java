package com.marciliojr.pirangueiro;

import com.marciliojr.pirangueiro.dto.BackupDTO;
import com.marciliojr.pirangueiro.service.BackupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração para o serviço de backup.
 * Demonstra o funcionamento básico das funcionalidades.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BackupServiceTest {

    @Autowired
    private BackupService backupService;

    @Test
    public void testGerarBackupCompleto() {
        // Gerar backup
        BackupDTO backup = backupService.gerarBackupCompleto();
        
        // Verificar metadados
        assertNotNull(backup);
        assertNotNull(backup.getDataGeracao());
        assertEquals("1.0", backup.getVersao());
        assertEquals("Pirangueiro v1.0", backup.getSistemaVersao());
        assertNotNull(backup.getTotalRegistros());
        
        // Verificar se as listas foram inicializadas (mesmo que vazias)
        assertNotNull(backup.getUsuarios());
        assertNotNull(backup.getCategorias());
        assertNotNull(backup.getContas());
        assertNotNull(backup.getCartoes());
        assertNotNull(backup.getPensamentos());
        assertNotNull(backup.getLimitesGastos());
        assertNotNull(backup.getGraficos());
        assertNotNull(backup.getExecucoesTarefas());
        assertNotNull(backup.getDespesas());
        assertNotNull(backup.getReceitas());
        assertNotNull(backup.getNotificacoes());
        assertNotNull(backup.getHistoricos());
        
        System.out.println("✅ Backup gerado com sucesso!");
        System.out.println("📊 Total de registros: " + backup.getTotalRegistros());
        System.out.println("📅 Data de geração: " + backup.getDataGeracao());
    }

    @Test
    public void testSerializacaoDesserializacao() throws Exception {
        // Gerar backup
        BackupDTO backupOriginal = backupService.gerarBackupCompleto();
        
        // Serializar para JSON
        String json = backupService.serializarBackup(backupOriginal);
        assertNotNull(json);
        assertFalse(json.isEmpty());
        
        // Deserializar de volta
        BackupDTO backupRestaurado = backupService.deserializarBackup(json);
        
        // Verificar se os dados foram preservados
        assertEquals(backupOriginal.getVersao(), backupRestaurado.getVersao());
        assertEquals(backupOriginal.getSistemaVersao(), backupRestaurado.getSistemaVersao());
        assertEquals(backupOriginal.getTotalRegistros(), backupRestaurado.getTotalRegistros());
        
        System.out.println("✅ Serialização/Deserialização funcionando!");
        System.out.println("📄 Tamanho do JSON: " + json.length() + " caracteres");
    }

    @Test
    public void testLimpezaTabelas() {
        // Limpar todas as tabelas
        backupService.limparTodasTabelas();
        
        // Gerar backup após limpeza
        BackupDTO backup = backupService.gerarBackupCompleto();
        
        // Verificar se todas as listas estão vazias
        assertEquals(0, backup.getUsuarios().size());
        assertEquals(0, backup.getCategorias().size());
        assertEquals(0, backup.getContas().size());
        assertEquals(0, backup.getCartoes().size());
        assertEquals(0, backup.getPensamentos().size());
        assertEquals(0, backup.getLimitesGastos().size());
        assertEquals(0, backup.getGraficos().size());
        assertEquals(0, backup.getExecucoesTarefas().size());
        assertEquals(0, backup.getDespesas().size());
        assertEquals(0, backup.getReceitas().size());
        assertEquals(0, backup.getNotificacoes().size());
        assertEquals(0, backup.getHistoricos().size());
        assertEquals(0, backup.getTotalRegistros());
        
        System.out.println("✅ Limpeza das tabelas funcionando!");
        System.out.println("🧹 Todas as tabelas foram limpas com sucesso");
    }

    @Test
    public void testRestauracaoBackupVazio() {
        // Gerar backup vazio
        BackupDTO backupVazio = backupService.gerarBackupCompleto();
        
        // Restaurar backup vazio
        assertDoesNotThrow(() -> backupService.restaurarBackup(backupVazio));
        
        // Verificar se o sistema continua funcionando
        BackupDTO novoBackup = backupService.gerarBackupCompleto();
        assertNotNull(novoBackup);
        
        System.out.println("✅ Restauração de backup vazio funcionando!");
    }
} 