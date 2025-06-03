package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para backup da entidade ExecucaoTarefa.
 */
@Data
public class ExecucaoTarefaBackupDTO {
    private Long id;
    private String nomeTarefa;
    private LocalDate dataExecucao;
} 