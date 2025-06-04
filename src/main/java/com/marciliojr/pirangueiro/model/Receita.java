package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Entidade que representa uma receita financeira.
 */
@Entity
@Data
public class Receita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private Double valor;

    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    @Lob
    @Column(name = "anexo", columnDefinition = "LONGBLOB")
    private byte[] anexo;

    private String observacao;

    private String extensaoAnexo;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Receita{");
        sb.append("id=").append(id);
        sb.append(", descricao='").append(descricao).append('\'');
        sb.append(", valor=").append(valor);
        sb.append(", data=").append(data);
        if (conta != null) {
            sb.append(", conta='").append(conta.getNome()).append('\'');
        }
        if (categoria != null) {
            sb.append(", categoria='").append(categoria.getNome()).append('\'');
        }
        if (observacao != null && !observacao.trim().isEmpty()) {
            sb.append(", observacao='").append(observacao).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
} 