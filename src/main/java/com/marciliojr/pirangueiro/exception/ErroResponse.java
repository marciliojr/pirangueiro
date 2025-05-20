package com.marciliojr.pirangueiro.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErroResponse {
    private String mensagem;
    private String codigo;
    private String detalhe;
    private LocalDateTime timestamp;

    public ErroResponse(String mensagem, String codigo, String detalhe) {
        this.mensagem = mensagem;
        this.codigo = codigo;
        this.detalhe = detalhe;
        this.timestamp = LocalDateTime.now();
    }

}

