package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO para backup da entidade Historico.
 * Mantém referências por ID para os relacionamentos.
 */
@Data
public class HistoricoBackupDTO {
    private Long id;
    private String tipoOperacao; // Enum serializado como String
    private String entidade;
    private Long entidadeId;
    private Long usuarioId; // Referência para Usuario
    private String info;
    private LocalDateTime dataHora;
} 