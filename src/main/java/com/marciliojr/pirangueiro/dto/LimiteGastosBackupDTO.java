package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para backup da entidade LimiteGastos.
 */
@Data
public class LimiteGastosBackupDTO {
    private Long id;
    private String descricao;
    private Double valor;
    private LocalDate data;
} 