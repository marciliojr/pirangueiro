package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.GraficoReceitasDespesasDTO;
import com.marciliojr.pirangueiro.dto.GraficoVariacaoMensalDTO;
import com.marciliojr.pirangueiro.service.GraficosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graficos")
@CrossOrigin(origins = "*")
public class GraficosController {

    @Autowired
    private GraficosService graficosService;

    @GetMapping("/receitas-despesas")
    public ResponseEntity<GraficoReceitasDespesasDTO> buscarGraficoReceitasDespesas(
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return ResponseEntity.ok(graficosService.buscarDadosGraficoReceitasDespesas(mes, ano));
    }

    @GetMapping("/variacao-mensal-despesas")
    public ResponseEntity<GraficoVariacaoMensalDTO> buscarVariacaoMensalDespesas(
            @RequestParam Integer ano) {
        return ResponseEntity.ok(graficosService.buscarVariacaoMensalDespesas(ano));
    }
} 