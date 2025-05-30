package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.HistoricoDTO;
import com.marciliojr.pirangueiro.model.Historico;
import com.marciliojr.pirangueiro.model.Usuario;
import com.marciliojr.pirangueiro.service.HistoricoService;
import com.marciliojr.pirangueiro.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsável por gerenciar o histórico de operações do sistema.
 * 
 * <p>Este controller fornece endpoints para consultar o histórico de operações
 * realizadas no sistema, incluindo funcionalidades como:</p>
 * <ul>
 *   <li>Listagem completa do histórico</li>
 *   <li>Busca por usuário específico</li>
 *   <li>Busca por entidade e ID da entidade</li>
 *   <li>Busca por período de tempo</li>
 *   <li>Auditoria de operações CRUD</li>
 * </ul>
 * 
 * <p>O histórico é fundamental para auditoria e rastreabilidade das operações
 * realizadas no sistema, permitindo identificar quem fez o quê e quando.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Histórico de Operações", description = "APIs para consulta do histórico de operações do sistema")
@RestController
@RequestMapping("/api/historicos")
public class HistoricoController {

    /**
     * Serviço responsável pela lógica de negócio do histórico.
     */
    @Autowired
    private HistoricoService historicoService;

    /**
     * Serviço responsável pela lógica de negócio dos usuários.
     */
    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista todo o histórico de operações do sistema.
     * 
     * @return Lista de HistoricoDTO contendo todas as operações registradas
     */
    @Operation(
        summary = "Listar todo o histórico",
        description = "Retorna uma lista completa de todas as operações registradas no histórico do sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Histórico retornado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HistoricoDTO.class)
            )
        )
    })
    @GetMapping
    public List<HistoricoDTO> listarTodos() {
        return historicoService.listarTodos().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca histórico de operações de um usuário específico.
     * 
     * @param usuarioId ID do usuário para busca do histórico
     * @return ResponseEntity contendo lista de operações do usuário
     * @throws RuntimeException se o usuário não for encontrado
     */
    @Operation(
        summary = "Buscar histórico por usuário",
        description = "Retorna todas as operações realizadas por um usuário específico."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Histórico do usuário retornado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HistoricoDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        )
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<HistoricoDTO>> buscarPorUsuario(
            @Parameter(description = "ID do usuário para busca do histórico", required = true)
            @PathVariable Long usuarioId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        List<Historico> historicos = historicoService.buscarPorUsuario(usuario);
        return ResponseEntity.ok(historicos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Busca histórico de operações de uma entidade específica.
     * 
     * @param entidade Nome da entidade (ex: "Despesa", "Receita", "Usuario")
     * @param entidadeId ID da entidade específica
     * @return ResponseEntity contendo lista de operações da entidade
     */
    @Operation(
        summary = "Buscar histórico por entidade",
        description = "Retorna todas as operações realizadas em uma entidade específica " +
                     "(ex: todas as operações em uma despesa específica)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Histórico da entidade retornado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HistoricoDTO.class)
            )
        )
    })
    @GetMapping("/entidade/{entidade}/{entidadeId}")
    public ResponseEntity<List<HistoricoDTO>> buscarPorEntidade(
            @Parameter(description = "Nome da entidade (ex: Despesa, Receita)", required = true)
            @PathVariable String entidade,
            @Parameter(description = "ID da entidade específica", required = true)
            @PathVariable Long entidadeId) {
        List<Historico> historicos = historicoService.buscarPorEntidade(entidade, entidadeId);
        return ResponseEntity.ok(historicos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Busca histórico de um usuário em um período específico.
     * 
     * @param usuarioId ID do usuário
     * @param dataInicio Data e hora de início do período
     * @param dataFim Data e hora de fim do período
     * @return ResponseEntity contendo lista de operações do período
     */
    @Operation(
        summary = "Buscar histórico por usuário e período",
        description = "Retorna todas as operações realizadas por um usuário específico " +
                     "dentro de um período de tempo determinado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Histórico do período retornado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HistoricoDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Período inválido ou parâmetros incorretos",
            content = @Content
        )
    })
    @GetMapping("/usuario/{usuarioId}/periodo")
    public ResponseEntity<List<HistoricoDTO>> buscarPorUsuarioEPeriodo(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId,
            @Parameter(description = "Data e hora de início (formato: yyyy-MM-ddTHH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @Parameter(description = "Data e hora de fim (formato: yyyy-MM-ddTHH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        List<Historico> historicos = historicoService.buscarPorUsuarioEPeriodo(usuarioId, dataInicio, dataFim);
        return ResponseEntity.ok(historicos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Converte uma entidade Historico para HistoricoDTO.
     * 
     * @param historico Entidade Historico a ser convertida
     * @return HistoricoDTO correspondente
     */
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