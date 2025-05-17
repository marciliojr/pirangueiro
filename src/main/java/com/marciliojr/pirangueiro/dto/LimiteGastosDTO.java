package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LimiteGastosDTO {
    private Long id;
    private String descricao;
    private Double valor;
    private LocalDate data;
} 