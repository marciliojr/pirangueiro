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
    private String entidade; // "DESPESA", "RECEITA", "CONTA", "CARTAO", "CATEGORIA", "USUARIO", "PENSAMENTOS", "LIMITE_GASTOS", "NOTIFICACAO"

    @Column(nullable = false)
    private Long entidadeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(columnDefinition = "TEXT")
    private String info; // toString() da entidade para histórico completo

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        dataHora = LocalDateTime.now();
    }

    public enum TipoOperacao {
        // Operações de Criação
        CRIACAO_DESPESA,
        CRIACAO_RECEITA,
        CRIACAO_CONTA,
        CRIACAO_CARTAO,
        CRIACAO_CATEGORIA,
        CRIACAO_USUARIO,
        CRIACAO_PENSAMENTOS,
        CRIACAO_LIMITE_GASTOS,
        CRIACAO_NOTIFICACAO,
        
        // Operações de Edição
        EDICAO_DESPESA,
        EDICAO_RECEITA,
        EDICAO_CONTA,
        EDICAO_CARTAO,
        EDICAO_CATEGORIA,
        EDICAO_USUARIO,
        EDICAO_PENSAMENTOS,
        EDICAO_LIMITE_GASTOS,
        EDICAO_NOTIFICACAO,
        
        // Operações de Exclusão
        EXCLUSAO_DESPESA,
        EXCLUSAO_RECEITA,
        EXCLUSAO_CONTA,
        EXCLUSAO_CARTAO,
        EXCLUSAO_CATEGORIA,
        EXCLUSAO_USUARIO,
        EXCLUSAO_PENSAMENTOS,
        EXCLUSAO_LIMITE_GASTOS,
        EXCLUSAO_NOTIFICACAO
    }
} 