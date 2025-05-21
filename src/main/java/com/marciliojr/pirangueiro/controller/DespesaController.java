package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.DespesaService;
import com.marciliojr.pirangueiro.dto.DespesaDTO;
import com.marciliojr.pirangueiro.util.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/despesas")
public class DespesaController {

    @Autowired
    private DespesaService despesaService;

    @Autowired
    private PDFGenerator pdfGenerator;

    @GetMapping
    public List<DespesaDTO> listarTodas() {
        return despesaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DespesaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(despesaService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    public Page<DespesaDTO> buscar(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanhoPagina) {
        return despesaService.buscarComFiltros(descricao, mes, ano, pagina, tamanhoPagina);
    }

    @GetMapping("/buscar/sempaginar")
    public List<DespesaDTO> buscarSemPaginar(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        String descricaoLimpa = limparString(descricao);
        List<DespesaDTO> despesaDTOS = despesaService.buscarComFiltrosSemPaginar(descricaoLimpa, mes, ano);
        return despesaDTOS;
    }

    @GetMapping("/mes/{mes}/ano/{ano}")
    public List<DespesaDTO> buscarPorMesEAno(@PathVariable int mes, @PathVariable int ano) {
        return despesaService.buscarPorMesEAno(mes, ano);
    }

    @GetMapping("/mes/{mes}/ano/{ano}/pdf")
    public ResponseEntity<byte[]> gerarPDFPorMesEAno(@PathVariable int mes, @PathVariable int ano) {
        List<DespesaDTO> despesas = despesaService.buscarPorMesEAno(mes, ano);
        String titulo = String.format("Relatório de Despesas - %d/%d", mes, ano);
        
        try {
            byte[] pdfBytes = pdfGenerator.gerarPDFDespesas(despesas, titulo);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", String.format("despesas_%d_%d.pdf", mes, ano));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/buscar-filtro")
    public List<DespesaDTO> buscarPorDescricaoContaCartaoData(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long cartaoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        System.out.println("Data: " + data);
         return despesaService.buscarPorDescricaoContaCartaoData(descricao, contaId, cartaoId, data);
    }

    @PostMapping
    public ResponseEntity<DespesaDTO> salvar(@RequestBody DespesaDTO despesaDTO) {
        despesaDTO.setPago(Boolean.FALSE);
        return ResponseEntity.ok(despesaService.salvar(despesaDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DespesaDTO> atualizar(@PathVariable Long id, @RequestBody DespesaDTO despesaDTO) {
        despesaDTO.setId(id);
        return ResponseEntity.ok(despesaService.salvar(despesaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        despesaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Double> buscarTotalDespesas() {
        return ResponseEntity.ok(despesaService.buscarTotalDespesas());
    }

    @PutMapping("/despesas/{id}/pagar")
    public ResponseEntity<Void> marcarComoPaga(@PathVariable Long id) {
        despesaService.marcarDespesaComoPaga(id);
        return ResponseEntity.ok().build();
    }

    private String limparString(String texto) {
        if (texto == null) {
            return null;
        }
        // Remove caracteres de formatação como \t, \n, \r, \f e espaços extras
        return texto.replaceAll("[\\t\\n\\r\\f]+", " ")  // substitui caracteres de formatação por espaço
                .replaceAll("\\s+", " ")              // substitui múltiplos espaços por um único espaço
                .trim();                              // remove espaços do início e fim
    }
} 