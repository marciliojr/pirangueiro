package com.marciliojr.pirangueiro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DespesaMensalDTO {
    private String mes; // formato: "2024-01"
    private Double total;
} 