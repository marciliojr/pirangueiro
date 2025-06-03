package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa um cartão de crédito.
 */
@Entity
@Data
public class Cartao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Double limite;

    private Double limiteUsado;

    private Integer diaFechamento;

    private Integer diaVencimento;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cartao{");
        sb.append("id=").append(id);
        sb.append(", nome='").append(nome).append('\'');
        sb.append(", limite=").append(limite);
        sb.append(", limiteUsado=").append(limiteUsado);
        sb.append(", diaFechamento=").append(diaFechamento);
        sb.append(", diaVencimento=").append(diaVencimento);
        sb.append('}');
        return sb.toString();
    }
} 