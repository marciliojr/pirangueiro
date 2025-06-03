package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa uma categoria de receita ou despesa.
 */
@Entity
@Data
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String cor;

    private Boolean tipoReceita;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Categoria{");
        sb.append("id=").append(id);
        sb.append(", nome='").append(nome).append('\'');
        sb.append(", cor='").append(cor).append('\'');
        sb.append(", tipoReceita=").append(tipoReceita);
        sb.append('}');
        return sb.toString();
    }
} 