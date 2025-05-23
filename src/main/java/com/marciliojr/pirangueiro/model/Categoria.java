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
} 