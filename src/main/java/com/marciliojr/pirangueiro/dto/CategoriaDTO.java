package com.marciliojr.pirangueiro.dto;

import lombok.Data;

@Data
public class CategoriaDTO {
    private Long id;
    private String nome;
    private String cor;
    private String imagemCategoria;
    private Boolean tipoReceita;
} 