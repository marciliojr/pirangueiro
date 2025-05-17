package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.CartaoService;
import com.marciliojr.pirangueiro.service.DespesaService;
import com.marciliojr.pirangueiro.dto.CartaoDTO;
import com.marciliojr.pirangueiro.dto.DespesaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    @Autowired
    private CartaoService cartaoService;

    @Autowired
    private DespesaService despesaService;

    @GetMapping
    public List<CartaoDTO> listarTodos() {
        return cartaoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartaoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cartaoService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    public List<CartaoDTO> buscarPorNome(@RequestParam String nome) {
        return cartaoService.buscarPorNome(nome);
    }

    @PostMapping
    public ResponseEntity<CartaoDTO> salvar(@RequestBody CartaoDTO cartaoDTO) {
        return ResponseEntity.ok(cartaoService.salvar(cartaoDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartaoDTO> atualizar(@PathVariable Long id, @RequestBody CartaoDTO cartaoDTO) {
        cartaoDTO.setId(id);
        return ResponseEntity.ok(cartaoService.salvar(cartaoDTO));
    }

    @GetMapping("/{id}/despesas/fatura")
    public ResponseEntity<List<DespesaDTO>> buscarDespesasPorPeriodoFatura(
            @PathVariable Long id,
            @RequestParam int mes,
            @RequestParam int ano) {
        return ResponseEntity.ok(despesaService.buscarDespesasPorCartaoEPeriodoFatura(id, mes, ano));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        cartaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
} 