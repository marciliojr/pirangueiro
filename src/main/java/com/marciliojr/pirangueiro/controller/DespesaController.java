package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.DespesaService;
import com.marciliojr.pirangueiro.dto.DespesaDTO;
import com.marciliojr.pirangueiro.util.PDFGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller responsável por gerenciar operações relacionadas às despesas do sistema.
 * 
 * <p>Este controller fornece endpoints para operações CRUD (Create, Read, Update, Delete)
 * de despesas, incluindo funcionalidades avançadas como:</p>
 * <ul>
 *   <li>Busca com filtros e paginação</li>
 *   <li>Geração de relatórios em PDF</li>
 *   <li>Marcação de despesas como pagas</li>
 *   <li>Cálculo de totais</li>
 *   <li>Busca por múltiplos critérios (conta, cartão, data, etc.)</li>
 * </ul>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Despesas", description = "APIs para gerenciamento de despesas do sistema")
@RestController
@RequestMapping("/api/despesas")
public class DespesaController {

    /**
     * Serviço responsável pela lógica de negócio relacionada às despesas.
     */
    @Autowired
    private DespesaService despesaService;

    /**
     * Utilitário para geração de relatórios em PDF.
     */
    @Autowired
    private PDFGenerator pdfGenerator;

    /**
     * Lista todas as despesas cadastradas no sistema.
     * 
     * @return Lista de DespesaDTO contendo todas as despesas
     */
    @Operation(
        summary = "Listar todas as despesas",
        description = "Retorna uma lista completa de todas as despesas cadastradas no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de despesas retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DespesaDTO.class)
            )
        )
    })
    @GetMapping
    public List<DespesaDTO> listarTodas() {
        return despesaService.listarTodas();
    }

    /**
     * Busca uma despesa específica pelo seu ID.
     * 
     * @param id ID único da despesa a ser buscada
     * @return ResponseEntity contendo a DespesaDTO se encontrada
     * @throws RuntimeException se a despesa não for encontrada
     */
    @Operation(
        summary = "Buscar despesa por ID",
        description = "Retorna os detalhes de uma despesa específica baseada no seu ID único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Despesa encontrada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DespesaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Despesa não encontrada",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<DespesaDTO> buscarPorId(
            @Parameter(description = "ID único da despesa", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(despesaService.buscarPorId(id));
    }

    /**
     * Busca despesas com filtros e paginação.
     * 
     * @param descricao Filtro opcional por descrição da despesa
     * @param mes Filtro opcional por mês (1-12)
     * @param ano Filtro opcional por ano
     * @param pagina Número da página para paginação (padrão: 0)
     * @param tamanhoPagina Tamanho da página para paginação (padrão: 20)
     * @return Página de DespesaDTO com os resultados filtrados
     */
    @Operation(
        summary = "Buscar despesas com filtros e paginação",
        description = "Retorna uma página de despesas filtradas por descrição, mês e/ou ano, " +
                     "com suporte a paginação para melhor performance."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Despesas encontradas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping("/buscar")
    public Page<DespesaDTO> buscar(
            @Parameter(description = "Descrição da despesa para filtro")
            @RequestParam(required = false) String descricao,
            @Parameter(description = "Mês para filtro (1-12)")
            @RequestParam(required = false) Integer mes,
            @Parameter(description = "Ano para filtro")
            @RequestParam(required = false) Integer ano,
            @Parameter(description = "Número da página (começando em 0)")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "20") int tamanhoPagina) {
        return despesaService.buscarComFiltros(descricao, mes, ano, pagina, tamanhoPagina);
    }

    /**
     * Busca despesas com filtros sem paginação.
     * 
     * @param descricao Filtro opcional por descrição da despesa
     * @param mes Filtro opcional por mês (1-12)
     * @param ano Filtro opcional por ano
     * @return Lista completa de DespesaDTO com os resultados filtrados
     */
    @Operation(
        summary = "Buscar despesas com filtros sem paginação",
        description = "Retorna uma lista completa de despesas filtradas por descrição, mês e/ou ano, " +
                     "sem limitação de paginação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Despesas encontradas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DespesaDTO.class)
            )
        )
    })
    @GetMapping("/buscar/sempaginar")
    public List<DespesaDTO> buscarSemPaginar(
            @Parameter(description = "Descrição da despesa para filtro")
            @RequestParam(required = false) String descricao,
            @Parameter(description = "Mês para filtro (1-12)")
            @RequestParam(required = false) Integer mes,
            @Parameter(description = "Ano para filtro")
            @RequestParam(required = false) Integer ano) {
        String descricaoLimpa = limparString(descricao);
        List<DespesaDTO> despesaDTOS = despesaService.buscarComFiltrosSemPaginar(descricaoLimpa, mes, ano);
        return despesaDTOS;
    }

    /**
     * Busca despesas de um mês e ano específicos.
     * 
     * @param mes Mês para busca (1-12)
     * @param ano Ano para busca
     * @return Lista de DespesaDTO do período especificado
     */
    @Operation(
        summary = "Buscar despesas por mês e ano",
        description = "Retorna todas as despesas de um mês e ano específicos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Despesas do período encontradas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DespesaDTO.class)
            )
        )
    })
    @GetMapping("/mes/{mes}/ano/{ano}")
    public List<DespesaDTO> buscarPorMesEAno(
            @Parameter(description = "Mês (1-12)", required = true)
            @PathVariable int mes,
            @Parameter(description = "Ano", required = true)
            @PathVariable int ano) {
        return despesaService.buscarPorMesEAno(mes, ano);
    }

    /**
     * Cria uma nova despesa no sistema.
     * 
     * <p>A despesa é criada automaticamente como não paga (pago = false).</p>
     * 
     * @param despesaDTO Dados da despesa a ser criada
     * @return ResponseEntity contendo a DespesaDTO da despesa criada
     */
    @Operation(
        summary = "Criar nova despesa",
        description = "Cria uma nova despesa no sistema. A despesa é automaticamente " +
                     "marcada como não paga ao ser criada."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Despesa criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DespesaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<DespesaDTO> salvar(
            @Parameter(description = "Dados da despesa a ser criada", required = true)
            @RequestBody DespesaDTO despesaDTO) {
        despesaDTO.setPago(Boolean.FALSE);
        return ResponseEntity.ok(despesaService.salvar(despesaDTO));
    }

    /**
     * Atualiza os dados de uma despesa existente.
     * 
     * @param id ID da despesa a ser atualizada
     * @param despesaDTO Novos dados da despesa
     * @return ResponseEntity contendo a DespesaDTO atualizada
     */
    @Operation(
        summary = "Atualizar despesa",
        description = "Atualiza os dados de uma despesa existente no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Despesa atualizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DespesaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Despesa não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<DespesaDTO> atualizar(
            @Parameter(description = "ID da despesa a ser atualizada", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novos dados da despesa", required = true)
            @RequestBody DespesaDTO despesaDTO) {
        despesaDTO.setId(id);
        return ResponseEntity.ok(despesaService.salvar(despesaDTO));
    }

    /**
     * Remove uma despesa do sistema.
     * 
     * @param id ID da despesa a ser removida
     * @return ResponseEntity vazio confirmando a exclusão
     */
    @Operation(
        summary = "Excluir despesa",
        description = "Remove uma despesa do sistema baseada no seu ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Despesa excluída com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Despesa não encontrada",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da despesa a ser excluída", required = true)
            @PathVariable Long id) {
        despesaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calcula o valor total de todas as despesas.
     * 
     * @return ResponseEntity contendo o valor total das despesas
     */
    @Operation(
        summary = "Calcular total de despesas",
        description = "Retorna o valor total de todas as despesas cadastradas no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Total calculado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Double.class)
            )
        )
    })
    @GetMapping("/total")
    public ResponseEntity<Double> buscarTotalDespesas() {
        ResponseEntity<Double> ok = ResponseEntity.ok(despesaService.buscarTotalDespesas());
        return ok;
    }

    /**
     * Limpa caracteres de formatação de uma string.
     * 
     * <p>Remove caracteres como tabs, quebras de linha e espaços extras,
     * deixando apenas um espaço entre as palavras.</p>
     * 
     * @param texto Texto a ser limpo
     * @return Texto limpo ou null se o input for null
     */
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