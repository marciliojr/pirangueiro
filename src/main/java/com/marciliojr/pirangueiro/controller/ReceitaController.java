package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.ReceitaService;
import com.marciliojr.pirangueiro.dto.ReceitaDTO;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller responsável por gerenciar operações relacionadas às receitas do sistema.
 * 
 * <p>Este controller fornece endpoints para operações CRUD (Create, Read, Update, Delete)
 * de receitas, incluindo funcionalidades avançadas como:</p>
 * <ul>
 *   <li>Busca com filtros e paginação</li>
 *   <li>Geração de relatórios em PDF</li>
 *   <li>Busca por descrição</li>
 *   <li>Cálculo de totais</li>
 *   <li>Busca por mês e ano específicos</li>
 * </ul>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Receitas", description = "APIs para gerenciamento de receitas do sistema")
@RestController
@RequestMapping("/api/receitas")
public class ReceitaController {

    /**
     * Serviço responsável pela lógica de negócio relacionada às receitas.
     */
    @Autowired
    private ReceitaService receitaService;

    /**
     * Utilitário para geração de relatórios em PDF.
     */
    @Autowired
    private PDFGenerator pdfGenerator;

    /**
     * Lista todas as receitas cadastradas no sistema.
     * 
     * @return Lista de ReceitaDTO contendo todas as receitas
     */
    @Operation(
        summary = "Listar todas as receitas",
        description = "Retorna uma lista completa de todas as receitas cadastradas no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de receitas retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReceitaDTO.class)
            )
        )
    })
    @GetMapping
    public List<ReceitaDTO> listarTodas() {
        return receitaService.listarTodas();
    }

    /**
     * Busca uma receita específica pelo seu ID.
     * 
     * @param id ID único da receita a ser buscada
     * @return ResponseEntity contendo a ReceitaDTO se encontrada
     * @throws RuntimeException se a receita não for encontrada
     */
    @Operation(
        summary = "Buscar receita por ID",
        description = "Retorna os detalhes de uma receita específica baseada no seu ID único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Receita encontrada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReceitaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Receita não encontrada",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReceitaDTO> buscarPorId(
            @Parameter(description = "ID único da receita", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(receitaService.buscarPorId(id));
    }

    /**
     * Busca receitas por descrição.
     * 
     * @param descricao Descrição da receita para busca
     * @return Lista de ReceitaDTO que correspondem à descrição
     */
    @Operation(
        summary = "Buscar receitas por descrição",
        description = "Retorna uma lista de receitas que contêm a descrição especificada."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Receitas encontradas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReceitaDTO.class)
            )
        )
    })
    @GetMapping("/buscar")
    public List<ReceitaDTO> buscarPorDescricao(
            @Parameter(description = "Descrição da receita para busca", required = true)
            @RequestParam String descricao) {
        return receitaService.buscarPorDescricao(descricao);
    }

    /**
     * Busca receitas com filtros avançados e paginação.
     * 
     * @param descricao Filtro opcional por descrição da receita
     * @param mes Filtro opcional por mês (1-12)
     * @param ano Filtro opcional por ano
     * @param pagina Número da página para paginação (padrão: 0)
     * @param tamanhoPagina Tamanho da página para paginação (padrão: 20)
     * @param ordenacao Campo para ordenação (opcional)
     * @param direcao Direção da ordenação: ASC ou DESC (padrão: DESC)
     * @return Página de ReceitaDTO com os resultados filtrados
     */
    @Operation(
        summary = "Buscar receitas com filtros avançados e paginação",
        description = "Retorna uma página de receitas filtradas por descrição, mês e/ou ano, " +
                     "com suporte a paginação e ordenação customizada."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Receitas encontradas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping("/filtros")
    public Page<ReceitaDTO> buscarComFiltros(
            @Parameter(description = "Descrição da receita para filtro")
            @RequestParam(required = false) String descricao,
            @Parameter(description = "Mês para filtro (1-12)")
            @RequestParam(required = false) Integer mes,
            @Parameter(description = "Ano para filtro")
            @RequestParam(required = false) Integer ano,
            @Parameter(description = "Número da página (começando em 0)")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "20") int tamanhoPagina,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(required = false) String ordenacao,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)")
            @RequestParam(required = false, defaultValue = "DESC") String direcao) {
        return receitaService.buscarComFiltros(
                descricao, mes, ano, pagina, tamanhoPagina, ordenacao, direcao);
    }

    /**
     * Busca receitas de um mês e ano específicos.
     * 
     * @param mes Mês para busca (1-12)
     * @param ano Ano para busca
     * @return Lista de ReceitaDTO do período especificado
     */
    @Operation(
        summary = "Buscar receitas por mês e ano",
        description = "Retorna todas as receitas de um mês e ano específicos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Receitas do período encontradas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReceitaDTO.class)
            )
        )
    })
    @GetMapping("/mes/{mes}/ano/{ano}")
    public List<ReceitaDTO> buscarPorMesEAno(
            @Parameter(description = "Mês (1-12)", required = true)
            @PathVariable int mes,
            @Parameter(description = "Ano", required = true)
            @PathVariable int ano) {
        return receitaService.buscarPorMesEAno(mes, ano);
    }

    /**
     * Gera um relatório em PDF das receitas de um mês e ano específicos.
     * 
     * @param mes Mês para o relatório (1-12)
     * @param ano Ano para o relatório
     * @return ResponseEntity contendo o PDF gerado como array de bytes
     */
    @Operation(
        summary = "Gerar relatório PDF de receitas por mês e ano",
        description = "Gera e retorna um relatório em PDF contendo todas as receitas " +
                     "de um mês e ano específicos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "PDF gerado com sucesso",
            content = @Content(
                mediaType = "application/pdf"
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno na geração do PDF",
            content = @Content
        )
    })
    @GetMapping("/mes/{mes}/ano/{ano}/pdf")
    public ResponseEntity<byte[]> gerarPDFPorMesEAno(
            @Parameter(description = "Mês (1-12)", required = true)
            @PathVariable int mes,
            @Parameter(description = "Ano", required = true)
            @PathVariable int ano) {
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

    /**
     * Cria uma nova receita no sistema.
     * 
     * @param receitaDTO Dados da receita a ser criada
     * @return ResponseEntity contendo a ReceitaDTO da receita criada
     */
    @Operation(
        summary = "Criar nova receita",
        description = "Cria uma nova receita no sistema com os dados fornecidos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Receita criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReceitaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<ReceitaDTO> salvar(
            @Parameter(description = "Dados da receita a ser criada", required = true)
            @RequestBody ReceitaDTO receitaDTO) {
        return ResponseEntity.ok(receitaService.salvar(receitaDTO));
    }

    /**
     * Atualiza os dados de uma receita existente.
     * 
     * @param id ID da receita a ser atualizada
     * @param receitaDTO Novos dados da receita
     * @return ResponseEntity contendo a ReceitaDTO atualizada
     */
    @Operation(
        summary = "Atualizar receita",
        description = "Atualiza os dados de uma receita existente no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Receita atualizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReceitaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Receita não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReceitaDTO> atualizar(
            @Parameter(description = "ID da receita a ser atualizada", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novos dados da receita", required = true)
            @RequestBody ReceitaDTO receitaDTO) {
        receitaDTO.setId(id);
        return ResponseEntity.ok(receitaService.salvar(receitaDTO));
    }

    /**
     * Remove uma receita do sistema.
     * 
     * @param id ID da receita a ser removida
     * @return ResponseEntity vazio confirmando a exclusão
     */
    @Operation(
        summary = "Excluir receita",
        description = "Remove uma receita do sistema baseada no seu ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Receita excluída com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Receita não encontrada",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da receita a ser excluída", required = true)
            @PathVariable Long id) {
        receitaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calcula o valor total de todas as receitas.
     * 
     * @return ResponseEntity contendo o valor total das receitas
     */
    @Operation(
        summary = "Calcular total de receitas",
        description = "Retorna o valor total de todas as receitas cadastradas no sistema."
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
    public ResponseEntity<Double> buscarTotalReceitas() {
        return ResponseEntity.ok(receitaService.buscarTotalReceitas());
    }
} 