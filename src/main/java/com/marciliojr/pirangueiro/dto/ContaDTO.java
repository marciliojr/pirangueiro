package com.marciliojr.pirangueiro.dto;

import com.marciliojr.pirangueiro.model.TipoConta;
import lombok.Data;

@Data
public class ContaDTO {
    private Long id;
    private String nome;
    private TipoConta tipo;
    private byte[] imagemLogo;
    private UsuarioDTO usuario;
} 