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
 *   <li>Operações administrativas de notificações</li>
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

} 