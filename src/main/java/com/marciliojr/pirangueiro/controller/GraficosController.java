package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.DashboardFinanceiroDTO;
import com.marciliojr.pirangueiro.dto.GraficoReceitasDespesasCategoriaDTO;
import com.marciliojr.pirangueiro.dto.GraficoDespesasCartaoDTO;
import com.marciliojr.pirangueiro.dto.GraficoSazonalidadeGastosDTO;
import com.marciliojr.pirangueiro.dto.GraficoTendenciaGastosDTO;
import com.marciliojr.pirangueiro.dto.GraficoReceitasDespesasResponseDTO;
import com.marciliojr.pirangueiro.service.GraficosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/graficos")
@CrossOrigin(origins = "*")
public class GraficosController {

    @Autowired
    private GraficosService graficosService;

    @GetMapping("/receitas-despesas-categoria")
    public ResponseEntity<GraficoReceitasDespesasCategoriaDTO> buscarGraficoReceitasDespesas(
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return ResponseEntity.ok(graficosService.buscarDadosGraficoReceitasDespesasCategoria(mes, ano));
    }

    @GetMapping("/dashboard-financeiro")
    public ResponseEntity<DashboardFinanceiroDTO> getDashboardFinanceiro(
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return ResponseEntity.ok(graficosService.getDashboardFinanceiro(mes, ano));
    }

    @GetMapping("/despesas-por-cartao")
    public ResponseEntity<GraficoDespesasCartaoDTO> buscarDespesasPorCartao(
            @RequestParam(defaultValue = "12") Integer mesesAtras) {
        return ResponseEntity.ok(graficosService.buscarDespesasPorCartaoAoLongoDoTempo(mesesAtras));
    }

    @GetMapping("/sazonalidade-gastos")
    public ResponseEntity<GraficoSazonalidadeGastosDTO> buscarSazonalidadeGastos() {
        return ResponseEntity.ok(graficosService.buscarSazonalidadeGastos());
    }

    @GetMapping("/tendencia-gastos")
    public ResponseEntity<GraficoTendenciaGastosDTO> buscarTendenciaGastos() {
        return ResponseEntity.ok(graficosService.buscarTendenciaGastos());
    }

    @GetMapping("/receitas-despesas")
    public ResponseEntity<GraficoReceitasDespesasResponseDTO> buscarGraficoReceitasDespesasPorMes(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        try {
            GraficoReceitasDespesasResponseDTO resultado = 
                graficosService.buscarGraficoReceitasDespesasPorMes(dataInicio, dataFim);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 