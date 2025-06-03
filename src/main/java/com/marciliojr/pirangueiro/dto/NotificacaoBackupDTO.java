package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO para backup da entidade Notificacao.
 * Mantém referências por ID para os relacionamentos.
 */
@Data
public class NotificacaoBackupDTO {
    private Long id;
    private String mensagem;
    private LocalDateTime dataGeracao;
    private Boolean lida;
    private Long cartaoId; // Referência para Cartao
} 