package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReceitaDTO {
    private Long id;
    private String descricao;
    private Double valor;
    private LocalDate data;
    private ContaDTO conta;
    private CategoriaDTO categoria;
    private byte[] anexo;
    private String observacao;
    private String extensaoAnexo;
} 