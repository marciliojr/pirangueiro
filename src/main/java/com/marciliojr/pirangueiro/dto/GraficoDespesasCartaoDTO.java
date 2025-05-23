package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GraficoDespesasCartaoDTO {
    private List<String> meses; // Lista de meses no formato "MM/YYYY"
    private List<SerieCartaoDTO> series; // Uma série para cada cartão
    private Double valorTotalPeriodo; // Valor total de todas as despesas no período
    
    @Data
    public static class SerieCartaoDTO {
        private String nomeCartao;
        private List<Double> valores; // Valores mensais para este cartão
        private Double valorTotal; // Valor total gasto no cartão no período
    }
} 