package com.marciliojr.pirangueiro.config;

import com.marciliojr.pirangueiro.exception.ErroResponse;
import com.marciliojr.pirangueiro.exception.NegocioException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErroResponse> handleNegocioException(NegocioException ex) {
        ErroResponse erro = new ErroResponse(
                ex.getMessage(),
                ex.getCodigo(),
                ex.getDetalhe()
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroResponse> handleRuntimeException(RuntimeException ex) {
        ErroResponse erro = new ErroResponse(
                ex.getMessage(),
                "ERRO_RUNTIME",
                null
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleException(Exception ex) {
        ErroResponse erro = new ErroResponse(
                "Ocorreu um erro interno no servidor",
                "ERRO_INTERNO",
                null
        );
        return ResponseEntity.internalServerError().body(erro);
    }
}
