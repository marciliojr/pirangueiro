package com.marciliojr.pirangueiro.dto;

import lombok.Data;

@Data
public class CartaoLimiteDTO {
    private String nomeCartao;
    private Double limiteTotal;
    private Double limiteUsado;
    private Double limiteDisponivel;
    private Double percentualUtilizado;
} 