package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa um gráfico.
 * Esta é uma entidade auxiliar usada apenas para organizar as queries de gráficos.
 */
@Entity
@Data
public class Grafico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Campos auxiliares para identificação
    private String nome;
    private String tipo;
} 