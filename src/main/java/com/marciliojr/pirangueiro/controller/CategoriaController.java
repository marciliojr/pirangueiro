package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.CategoriaService;
import com.marciliojr.pirangueiro.dto.CategoriaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaDTO> listarTodas() {
        List<CategoriaDTO> categoriaDTOS = categoriaService.listarTodas();
        return categoriaDTOS;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    public List<CategoriaDTO> buscarPorNome(@RequestParam String nome) {
        return categoriaService.buscarPorNome(nome);
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> salvar(@RequestBody CategoriaDTO categoriaDTO) {
        return ResponseEntity.ok(categoriaService.salvar(categoriaDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> atualizar(@PathVariable Long id, @RequestBody CategoriaDTO categoriaDTO) {
        categoriaDTO.setId(id);
        return ResponseEntity.ok(categoriaService.salvar(categoriaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/receitas")
    public List<CategoriaDTO> listarReceitas() {
        return categoriaService.buscarPorTipoReceita(true);
    }

    @GetMapping("/despesas")
    public List<CategoriaDTO> listarDespesas() {
        return categoriaService.buscarPorTipoReceita(false);
    }
} 