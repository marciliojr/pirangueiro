package com.marciliojr.pirangueiro.dto;

import lombok.Data;

/**
 * DTO para backup da entidade Usuario.
 */
@Data
public class UsuarioBackupDTO {
    private Long id;
    private String nome;
    private String senha;
} 