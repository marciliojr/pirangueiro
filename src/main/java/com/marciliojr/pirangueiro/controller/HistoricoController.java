package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.HistoricoDTO;
import com.marciliojr.pirangueiro.model.Historico;
import com.marciliojr.pirangueiro.model.Usuario;
import com.marciliojr.pirangueiro.service.HistoricoService;
import com.marciliojr.pirangueiro.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/historicos")
public class HistoricoController {

    @Autowired
    private HistoricoService historicoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<HistoricoDTO> listarTodos() {
        return historicoService.listarTodos().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<HistoricoDTO>> buscarPorUsuario(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        List<Historico> historicos = historicoService.buscarPorUsuario(usuario);
        return ResponseEntity.ok(historicos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/entidade/{entidade}/{entidadeId}")
    public ResponseEntity<List<HistoricoDTO>> buscarPorEntidade(
            @PathVariable String entidade, 
            @PathVariable Long entidadeId) {
        List<Historico> historicos = historicoService.buscarPorEntidade(entidade, entidadeId);
        return ResponseEntity.ok(historicos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/usuario/{usuarioId}/periodo")
    public ResponseEntity<List<HistoricoDTO>> buscarPorUsuarioEPeriodo(
            @PathVariable Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        List<Historico> historicos = historicoService.buscarPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim);
        return ResponseEntity.ok(historicos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    private HistoricoDTO convertToDTO(Historico historico) {
        HistoricoDTO dto = new HistoricoDTO();
        dto.setId(historico.getId());
        dto.setTipoOperacao(historico.getTipoOperacao());
        dto.setEntidade(historico.getEntidade());
        dto.setEntidadeId(historico.getEntidadeId());
        dto.setNomeUsuario(historico.getUsuario().getNome());
        dto.setDataHora(historico.getDataHora());
        return dto;
    }
} 