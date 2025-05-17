package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.ReceitaService;
import com.marciliojr.pirangueiro.dto.ReceitaDTO;
import com.marciliojr.pirangueiro.util.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/receitas")
public class ReceitaController {

    @Autowired
    private ReceitaService receitaService;

    @Autowired
    private PDFGenerator pdfGenerator;

    @GetMapping
    public List<ReceitaDTO> listarTodas() {
        return receitaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceitaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(receitaService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    public List<ReceitaDTO> buscarPorDescricao(@RequestParam String descricao) {
        return receitaService.buscarPorDescricao(descricao);
    }

    @GetMapping("/mes/{mes}/ano/{ano}")
    public List<ReceitaDTO> buscarPorMesEAno(@PathVariable int mes, @PathVariable int ano) {
        return receitaService.buscarPorMesEAno(mes, ano);
    }

    @GetMapping("/mes/{mes}/ano/{ano}/pdf")
    public ResponseEntity<byte[]> gerarPDFPorMesEAno(@PathVariable int mes, @PathVariable int ano) {
        List<ReceitaDTO> receitas = receitaService.buscarPorMesEAno(mes, ano);
        String titulo = String.format("Relatório de Receitas - %d/%d", mes, ano);
        
        try {
            byte[] pdfBytes = pdfGenerator.gerarPDFReceitas(receitas, titulo);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", String.format("receitas_%d_%d.pdf", mes, ano));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<ReceitaDTO> salvar(@RequestBody ReceitaDTO receitaDTO) {
        return ResponseEntity.ok(receitaService.salvar(receitaDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceitaDTO> atualizar(@PathVariable Long id, @RequestBody ReceitaDTO receitaDTO) {
        receitaDTO.setId(id);
        return ResponseEntity.ok(receitaService.salvar(receitaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        receitaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Double> buscarTotalReceitas() {
        return ResponseEntity.ok(receitaService.buscarTotalReceitas());
    }
} 