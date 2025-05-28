package com.marciliojr.pirangueiro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraficoReceitasDespesasResponseDTO {
    private List<GraficoReceitasDespesasDTO> dados;
    private PeriodoDTO periodo;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodoDTO {
        private LocalDate dataInicio;
        private LocalDate dataFim;
    }
} 