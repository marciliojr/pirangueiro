package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.util.List;

@Data
public class GraficoReceitasDespesasDTO {
    private Integer mes;
    private Integer ano;
    private List<DadosGraficoDTO> receitas;
    private List<DadosGraficoDTO> despesas;
    private Double totalReceitas;
    private Double totalDespesas;
    private Double saldo;
} 