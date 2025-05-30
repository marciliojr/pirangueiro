package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.LimiteGastosService;
import com.marciliojr.pirangueiro.dto.LimiteGastosDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller responsável por gerenciar limites de gastos do sistema.
 * 
 * <p>Este controller fornece endpoints para consultar e gerenciar limites de gastos
 * configurados pelos usuários. Os limites de gastos são utilizados para monitorar
 * e alertar sobre gastos excessivos em categorias específicas ou no total.</p>
 * 
 * <p>As funcionalidades incluem:</p>
 * <ul>
 *   <li>Listagem de todos os limites configurados</li>
 *   <li>Monitoramento de gastos vs limites</li>
 *   <li>Geração de alertas quando limites são ultrapassados</li>
 * </ul>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Limites de Gastos", description = "APIs para gerenciamento de limites de gastos")
@RestController
@RequestMapping("/api/limites")
public class LimiteGastosController {

    /**
     * Serviço responsável pela lógica de negócio dos limites de gastos.
     */
    @Autowired
    private LimiteGastosService limiteGastosService;

    /**
     * Lista todos os limites de gastos configurados.
     * 
     * @return Lista de LimiteGastosDTO contendo todos os limites configurados
     */
    @Operation(
        summary = "Listar todos os limites de gastos",
        description = "Retorna uma lista completa de todos os limites de gastos configurados " +
                     "no sistema, incluindo valores definidos e gastos atuais para comparação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de limites retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LimiteGastosDTO.class)
            )
        )
    })
    @GetMapping
    public List<LimiteGastosDTO> listarTodos() {
        return limiteGastosService.listarTodos();
    }
} 