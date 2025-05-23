package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.util.List;

@Data
public class GraficoSazonalidadeGastosDTO {
    private List<String> meses; // Lista dos 12 meses
    private List<Double> mediasGastos; // Média de gastos para cada mês
    private Double maiorMedia; // Maior média mensal
    private Double menorMedia; // Menor média mensal
    private String mesMaiorGasto; // Mês com maior gasto médio
    private String mesMenorGasto; // Mês com menor gasto médio
} 