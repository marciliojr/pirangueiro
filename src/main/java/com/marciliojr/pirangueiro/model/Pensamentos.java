package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa um pensamento motivacional.
 */
@Entity
@Data
public class Pensamentos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String texto;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pensamentos{");
        sb.append("id=").append(id);
        sb.append(", texto='").append(texto != null && texto.length() > 50 
            ? texto.substring(0, 50) + "..." 
            : texto).append('\'');
        sb.append('}');
        return sb.toString();
    }
} 