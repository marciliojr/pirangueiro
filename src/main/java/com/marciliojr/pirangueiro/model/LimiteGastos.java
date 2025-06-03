package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Entidade que representa um limite de gastos.
 */
@Entity
@Data
public class LimiteGastos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private Double valor;

    private LocalDate data;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LimiteGastos{");
        sb.append("id=").append(id);
        sb.append(", descricao='").append(descricao).append('\'');
        sb.append(", valor=").append(valor);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
} 