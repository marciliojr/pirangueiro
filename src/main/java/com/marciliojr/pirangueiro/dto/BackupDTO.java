package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO principal para backup completo do sistema.
 * Contém todas as entidades e metadados do backup.
 */
@Data
public class BackupDTO {
    
    // Metadados do backup
    private LocalDateTime dataGeracao;
    private String versao;
    private String sistemaVersao;
    private Integer totalRegistros;
    
    // Entidades independentes (sem relacionamentos)
    private List<UsuarioBackupDTO> usuarios;
    private List<CategoriaBackupDTO> categorias;
    private List<ContaBackupDTO> contas;
    private List<CartaoBackupDTO> cartoes;
    private List<PensamentosBackupDTO> pensamentos;
    private List<LimiteGastosBackupDTO> limitesGastos;
    private List<GraficoBackupDTO> graficos;
    private List<ExecucaoTarefaBackupDTO> execucoesTarefas;
    
    // Entidades com relacionamentos (devem ser restauradas após as independentes)
    private List<DespesaBackupDTO> despesas;
    private List<ReceitaBackupDTO> receitas;
    private List<NotificacaoBackupDTO> notificacoes;
    private List<HistoricoBackupDTO> historicos;
} 