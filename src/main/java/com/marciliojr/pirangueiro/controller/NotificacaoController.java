package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.model.Notificacao;
import com.marciliojr.pirangueiro.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável por gerenciar notificações do sistema.
 * 
 * <p>Este controller fornece endpoints para gerenciamento das notificações,
 * incluindo funcionalidades como:</p>
 * <ul>
 *   <li>Buscar notificações não lidas</li>
 *   <li>Marcar notificações como lidas</li>
 * </ul>
 * 
 * <p>As notificações são utilizadas para alertar usuários sobre eventos
 * importantes do sistema, como limites de gastos ultrapassados, vencimentos, etc.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Notificações", description = "APIs para gerenciamento de notificações do sistema")
@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    /**
     * Serviço responsável pela lógica de negócio das notificações.
     */
    private final NotificacaoService notificacaoService;

    /**
     * Busca todas as notificações não lidas.
     * 
     * @return ResponseEntity contendo lista de notificações não lidas
     */
    @Operation(
        summary = "Buscar notificações não lidas",
        description = "Retorna uma lista de todas as notificações que ainda não foram " +
                     "marcadas como lidas pelo usuário."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notificações não lidas retornadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Notificacao.class)
            )
        )
    })
    @GetMapping("/nao-lidas")
    public ResponseEntity<List<Notificacao>> buscarNotificacoesNaoLidas() {
        return ResponseEntity.ok(notificacaoService.buscarNotificacoesNaoLidas());
    }

    /**
     * Marca uma notificação específica como lida.
     * 
     * @param id ID da notificação a ser marcada como lida
     * @return ResponseEntity vazio confirmando a operação
     */
    @Operation(
        summary = "Marcar notificação como lida",
        description = "Atualiza o status de uma notificação específica para 'lida', " +
                     "removendo-a da lista de notificações pendentes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notificação marcada como lida com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Notificação não encontrada",
            content = @Content
        )
    })
    @PatchMapping("/{id}/marcar-como-lida")
    public ResponseEntity<Void> marcarComoLida(
            @Parameter(description = "ID da notificação a ser marcada como lida", required = true)
            @PathVariable Long id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.ok().build();
    }
} 