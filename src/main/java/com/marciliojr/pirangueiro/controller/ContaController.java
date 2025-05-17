package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.ContaService;
import com.marciliojr.pirangueiro.dto.ContaDTO;
import com.marciliojr.pirangueiro.dto.SaldoContaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contas")
@CrossOrigin(origins = "*")
public class ContaController {

    @Autowired
    private ContaService contaService;

    @GetMapping
    public ResponseEntity<Object> listarTodas() {
        return ResponseEntity.ok(contaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<Object> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(contaService.buscarPorNome(nome));
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<Object> salvar(
            @RequestPart("conta") ContaDTO contaDTO,
            @RequestPart(value = "imagemLogo", required = false) MultipartFile imagemLogo) {
        return ResponseEntity.ok(contaService.salvar(contaDTO, imagemLogo));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<ContaDTO> atualizar(
            @PathVariable Long id,
            @RequestPart("conta") ContaDTO contaDTO,
            @RequestPart(value = "imagemLogo", required = false) MultipartFile imagemLogo) {
        contaDTO.setId(id);
        return ResponseEntity.ok(contaService.salvar(contaDTO, imagemLogo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        contaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity<SaldoContaDTO> calcularSaldo(
            @PathVariable Long id,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(contaService.calcularSaldoConta(id, mes, ano));
    }
} 