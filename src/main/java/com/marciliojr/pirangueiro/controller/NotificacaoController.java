package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.model.Notificacao;
import com.marciliojr.pirangueiro.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping("/nao-lidas")
    public ResponseEntity<List<Notificacao>> buscarNotificacoesNaoLidas() {
        return ResponseEntity.ok(notificacaoService.buscarNotificacoesNaoLidas());
    }

    @PatchMapping("/{id}/marcar-como-lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.ok().build();
    }
} 