package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardFinanceiroDTO {
    private Double saldoAtual;
    private Double taxaEconomiaMensal;
    private List<CartaoLimiteDTO> limitesCartoes;
} 