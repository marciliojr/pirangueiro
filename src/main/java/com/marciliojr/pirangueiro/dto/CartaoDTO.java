package com.marciliojr.pirangueiro.dto;

import lombok.Data;

@Data
public class CartaoDTO {
    private Long id;
    private String nome;
    private Double limite;
    private Double limiteUsado;
    private Integer diaFechamento;
    private Integer diaVencimento;
} 