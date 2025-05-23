package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.util.List;

@Data
public class GraficoTendenciaGastosDTO {
    private List<String> meses; // Lista dos últimos 12 meses no formato "MM/YYYY"
    private List<Double> valores; // Valores mensais de despesas
    private Double coeficienteAngular; // Coeficiente angular da reta de regressão (indica tendência)
    private Double mediaGastos; // Média de gastos no período
    private String tendencia; // "CRESCENTE", "DECRESCENTE" ou "ESTÁVEL"
    private Double valorPrevistoProximoMes; // Previsão para o próximo mês baseada na regressão
} 