package com.marciliojr.pirangueiro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraficoReceitasDespesasDTO {
    private String mes; // formato: "2024-01"
    private Double totalReceitas;
    private Double totalDespesas;
    private Double saldo; // receitas - despesas
    
    public GraficoReceitasDespesasDTO(String mes, Double totalReceitas, Double totalDespesas) {
        this.mes = mes;
        this.totalReceitas = totalReceitas != null ? totalReceitas : 0.0;
        this.totalDespesas = totalDespesas != null ? totalDespesas : 0.0;
        this.saldo = this.totalReceitas - this.totalDespesas;
    }
} 