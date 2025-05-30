package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa um usuário do sistema.
 */
@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private String senha;
} 