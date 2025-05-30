package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.CartaoService;
import com.marciliojr.pirangueiro.service.DespesaService;
import com.marciliojr.pirangueiro.dto.CartaoDTO;
import com.marciliojr.pirangueiro.dto.DespesaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller responsável por gerenciar operações relacionadas aos cartões de crédito do sistema.
 * 
 * <p>Este controller fornece endpoints para operações CRUD (Create, Read, Update, Delete)
 * de cartões de crédito, incluindo funcionalidades específicas como:</p>
 * <ul>
 *   <li>Busca por nome</li>
 *   <li>Consulta de despesas por período de fatura</li>
 *   <li>Cálculo de limite disponível</li>
 *   <li>Exclusão com opção de manter despesas</li>
 * </ul>
 * 
 * <p>Os cartões são utilizados para organizar despesas e controlar limites de crédito,
 * permitindo análises detalhadas de gastos por cartão.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Cartões de Crédito", description = "APIs para gerenciamento de cartões de crédito")
@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    /**
     * Serviço responsável pela lógica de negócio relacionada aos cartões.
     */
    @Autowired
    private CartaoService cartaoService;

    /**
     * Serviço responsável pela lógica de negócio relacionada às despesas.
     */
    @Autowired
    private DespesaService despesaService;

    /**
     * Lista todos os cartões cadastrados no sistema.
     * 
     * @return Lista de CartaoDTO contendo todos os cartões
     */
    @Operation(
        summary = "Listar todos os cartões",
        description = "Retorna uma lista completa de todos os cartões de crédito cadastrados no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de cartões retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartaoDTO.class)
            )
        )
    })
    @GetMapping
    public List<CartaoDTO> listarTodos() {
        return cartaoService.listarTodos();
    }

    /**
     * Busca um cartão específico pelo seu ID.
     * 
     * @param id ID único do cartão a ser buscado
     * @return ResponseEntity contendo o CartaoDTO se encontrado
     * @throws RuntimeException se o cartão não for encontrado
     */
    @Operation(
        summary = "Buscar cartão por ID",
        description = "Retorna os detalhes de um cartão específico baseado no seu ID único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cartão encontrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartaoDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cartão não encontrado",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CartaoDTO> buscarPorId(
            @Parameter(description = "ID único do cartão", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(cartaoService.buscarPorId(id));
    }

    /**
     * Busca cartões por nome.
     * 
     * @param nome Nome do cartão para busca (busca parcial)
     * @return Lista de CartaoDTO que correspondem ao nome
     */
    @Operation(
        summary = "Buscar cartões por nome",
        description = "Retorna uma lista de cartões que contêm o nome especificado. " +
                     "A busca é realizada de forma parcial (like)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cartões encontrados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartaoDTO.class)
            )
        )
    })
    @GetMapping("/buscar")
    public List<CartaoDTO> buscarPorNome(
            @Parameter(description = "Nome do cartão para busca", required = true)
            @RequestParam String nome) {
        return cartaoService.buscarPorNome(nome);
    }

    /**
     * Cria um novo cartão no sistema.
     * 
     * @param cartaoDTO Dados do cartão a ser criado
     * @return ResponseEntity contendo o CartaoDTO do cartão criado
     */
    @Operation(
        summary = "Criar novo cartão",
        description = "Cria um novo cartão de crédito no sistema com os dados fornecidos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cartão criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartaoDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<CartaoDTO> salvar(
            @Parameter(description = "Dados do cartão a ser criado", required = true)
            @RequestBody CartaoDTO cartaoDTO) {
        return ResponseEntity.ok(cartaoService.salvar(cartaoDTO));
    }

    /**
     * Atualiza os dados de um cartão existente.
     * 
     * @param id ID do cartão a ser atualizado
     * @param cartaoDTO Novos dados do cartão
     * @return ResponseEntity contendo o CartaoDTO atualizado
     */
    @Operation(
        summary = "Atualizar cartão",
        description = "Atualiza os dados de um cartão existente no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cartão atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartaoDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cartão não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CartaoDTO> atualizar(
            @Parameter(description = "ID do cartão a ser atualizado", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novos dados do cartão", required = true)
            @RequestBody CartaoDTO cartaoDTO) {
        return ResponseEntity.ok(cartaoService.atualizar(cartaoDTO));
    }

    /**
     * Busca despesas de um cartão por período de fatura.
     * 
     * @param id ID do cartão
     * @param mes Mês da fatura (1-12)
     * @param ano Ano da fatura
     * @return ResponseEntity contendo lista de despesas do período
     */
    @Operation(
        summary = "Buscar despesas por período de fatura",
        description = "Retorna todas as despesas de um cartão específico para um período " +
                     "de fatura (mês e ano), útil para fechamento de faturas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Despesas da fatura retornadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DespesaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cartão não encontrado",
            content = @Content
        )
    })
    @GetMapping("/{id}/despesas/fatura")
    public ResponseEntity<List<DespesaDTO>> buscarDespesasPorPeriodoFatura(
            @Parameter(description = "ID do cartão", required = true)
            @PathVariable Long id,
            @Parameter(description = "Mês da fatura (1-12)", required = true)
            @RequestParam int mes,
            @Parameter(description = "Ano da fatura", required = true)
            @RequestParam int ano) {
        return ResponseEntity.ok(despesaService.buscarDespesasPorCartaoEPeriodoFatura(id, mes, ano));
    }

    /**
     * Remove um cartão do sistema.
     * 
     * @param id ID do cartão a ser removido
     * @param manterDespesas Se true, mantém as despesas associadas; se false, remove tudo
     * @return ResponseEntity vazio confirmando a exclusão
     */
    @Operation(
        summary = "Excluir cartão",
        description = "Remove um cartão do sistema baseado no seu ID. " +
                     "Permite escolher se as despesas associadas devem ser mantidas ou removidas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cartão excluído com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cartão não encontrado",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do cartão a ser excluído", required = true)
            @PathVariable Long id,
            @Parameter(description = "Se deve manter as despesas associadas")
            @RequestParam(required = false) boolean manterDespesas) {
        cartaoService.excluir(id, manterDespesas);
        return ResponseEntity.noContent().build();
    }

    /**
     * Consulta o limite disponível de um cartão.
     * 
     * @param id ID do cartão para consulta
     * @return ResponseEntity contendo o valor do limite disponível
     */
    @Operation(
        summary = "Consultar limite disponível",
        description = "Calcula e retorna o limite disponível de um cartão, " +
                     "considerando o limite total menos os gastos atuais."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Limite disponível calculado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Double.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cartão não encontrado",
            content = @Content
        )
    })
    @GetMapping("/{id}/limite-disponivel")
    public ResponseEntity<Double> consultarLimiteDisponivel(
            @Parameter(description = "ID do cartão para consulta do limite", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(cartaoService.calcularLimiteDisponivel(id));
    }
} 