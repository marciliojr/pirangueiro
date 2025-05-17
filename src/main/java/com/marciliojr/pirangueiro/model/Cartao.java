package com.marciliojr.pirangueiro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
} 