package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidade que representa o histórico de operações do sistema.
 */
@Entity
@Data
public class Historico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private TipoOperacao tipoOperacao;

    @Column(nullable = false)
    private String entidade; // "DESPESA", "RECEITA", "CONTA", "CARTAO"

    @Column(nullable = false)
    private Long entidadeId;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        dataHora = LocalDateTime.now();
    }

    public enum TipoOperacao {
        CRIACAO_DESPESA,
        CRIACAO_RECEITA,
        CRIACAO_CONTA,
        CRIACAO_CARTAO,
        EDICAO_DESPESA,
        EDICAO_RECEITA,
        EDICAO_CONTA,
        EDICAO_CARTAO
    }
} 