package com.marciliojr.pirangueiro.dto;

import lombok.Data;

@Data
public class SaldoContaDTO {
    private Long contaId;
    private String nomeConta;
    private Double totalReceitas;
    private Double totalDespesas;
    private Double totalDespesasConta; // Despesas diretas da conta
    private Double totalDespesasCartao; // Despesas de cartão não pagas
    private Double saldo;
    private Integer mes;
    private Integer ano;
} 