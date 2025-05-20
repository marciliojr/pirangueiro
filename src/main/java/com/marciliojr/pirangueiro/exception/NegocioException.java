package com.marciliojr.pirangueiro.exception;

public class NegocioException extends RuntimeException {
    private final String codigo;
    private final String detalhe;

    public NegocioException(String mensagem) {
        super(mensagem);
        this.codigo = "NEGOCIO_ERRO";
        this.detalhe = null;
    }

    public NegocioException(String mensagem, String codigo, String detalhe) {
        super(mensagem);
        this.codigo = codigo;
        this.detalhe = detalhe;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDetalhe() {
        return detalhe;
    }
}

