package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.PensamentosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pensamentos")
public class PensamentosController {

    @Autowired
    private PensamentosService pensamentosService;

    @GetMapping("/obter-mensagem-dia")
    public ResponseEntity<String> obterMensagemDoDia() {
        String mensagem = pensamentosService.obterMensagemDoDia();
        return ResponseEntity.ok(mensagem);
    }
} 