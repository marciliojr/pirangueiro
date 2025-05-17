package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.LimiteGastosService;
import com.marciliojr.pirangueiro.dto.LimiteGastosDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/limites")
public class LimiteGastosController {

    @Autowired
    private LimiteGastosService limiteGastosService;

    @GetMapping
    public List<LimiteGastosDTO> listarTodos() {
        return limiteGastosService.listarTodos();
    }
} 