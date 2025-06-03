package com.marciliojr.pirangueiro.dto;

import lombok.Data;

/**
 * DTO para backup da entidade Cartao.
 */
@Data
public class CartaoBackupDTO {
    private Long id;
    private String nome;
    private Double limite;
    private Double limiteUsado;
    private Integer diaFechamento;
    private Integer diaVencimento;
} 