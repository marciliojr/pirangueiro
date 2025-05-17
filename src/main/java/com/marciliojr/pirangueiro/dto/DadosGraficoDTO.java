package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DadosGraficoDTO {
    private String categoria;
    private Double valor;
    private String cor; // Para personalização das cores no gráfico
    private Double percentual; // Percentual em relação ao total
} 