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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usuario{");
        sb.append("id=").append(id);
        sb.append(", nome='").append(nome).append('\'');
        // Não inclui a senha por questões de segurança
        sb.append('}');
        return sb.toString();
    }
} 