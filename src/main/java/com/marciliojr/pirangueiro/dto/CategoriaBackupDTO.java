package com.marciliojr.pirangueiro.dto;

import lombok.Data;

/**
 * DTO para backup da entidade Categoria.
 */
@Data
public class CategoriaBackupDTO {
    private Long id;
    private String nome;
    private String cor;
    private Boolean tipoReceita;
} 