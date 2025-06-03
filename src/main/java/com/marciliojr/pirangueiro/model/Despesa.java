package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Entidade que representa uma despesa financeira.
 */
@Entity
@Data
public class Despesa {
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
    @JoinColumn(name = "cartao_id")
    private Cartao cartao;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String anexo;

    private String observacao;

    @Column(nullable = true)
    private Integer numeroParcela;

    @Column(nullable = true)
    private Integer totalParcelas;

    @Column(nullable = true)
    private Boolean pago;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Despesa{");
        sb.append("id=").append(id);
        sb.append(", descricao='").append(descricao).append('\'');
        sb.append(", valor=").append(valor);
        sb.append(", data=").append(data);
        if (conta != null) {
            sb.append(", conta='").append(conta.getNome()).append('\'');
        }
        if (cartao != null) {
            sb.append(", cartao='").append(cartao.getNome()).append('\'');
        }
        if (categoria != null) {
            sb.append(", categoria='").append(categoria.getNome()).append('\'');
        }
        if (numeroParcela != null && totalParcelas != null) {
            sb.append(", parcela=").append(numeroParcela).append("/").append(totalParcelas);
        }
        sb.append(", pago=").append(pago);
        if (observacao != null && !observacao.trim().isEmpty()) {
            sb.append(", observacao='").append(observacao).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
} 