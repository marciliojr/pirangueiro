package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.CategoriaService;
import com.marciliojr.pirangueiro.dto.CategoriaDTO;
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
 * Controller responsável por gerenciar operações relacionadas às categorias do sistema.
 * 
 * <p>Este controller fornece endpoints para operações CRUD (Create, Read, Update, Delete)
 * de categorias, incluindo funcionalidades específicas como:</p>
 * <ul>
 *   <li>Busca por nome</li>
 *   <li>Listagem de categorias de receitas</li>
 *   <li>Listagem de categorias de despesas</li>
 *   <li>Separação entre categorias de receita e despesa</li>
 * </ul>
 * 
 * <p>As categorias são utilizadas para classificar e organizar tanto receitas quanto
 * despesas, permitindo análises e relatórios mais detalhados.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Categorias", description = "APIs para gerenciamento de categorias de receitas e despesas")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    /**
     * Serviço responsável pela lógica de negócio relacionada às categorias.
     */
    @Autowired
    private CategoriaService categoriaService;

    /**
     * Lista todas as categorias cadastradas no sistema.
     * 
     * @return Lista de CategoriaDTO contendo todas as categorias
     */
    @Operation(
        summary = "Listar todas as categorias",
        description = "Retorna uma lista completa de todas as categorias cadastradas no sistema, " +
                     "incluindo tanto categorias de receitas quanto de despesas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de categorias retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoriaDTO.class)
            )
        )
    })
    @GetMapping
    public List<CategoriaDTO> listarTodas() {
        List<CategoriaDTO> categoriaDTOS = categoriaService.listarTodas();
        return categoriaDTOS;
    }

    /**
     * Busca uma categoria específica pelo seu ID.
     * 
     * @param id ID único da categoria a ser buscada
     * @return ResponseEntity contendo a CategoriaDTO se encontrada
     * @throws RuntimeException se a categoria não for encontrada
     */
    @Operation(
        summary = "Buscar categoria por ID",
        description = "Retorna os detalhes de uma categoria específica baseada no seu ID único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoria encontrada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoriaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoria não encontrada",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscarPorId(
            @Parameter(description = "ID único da categoria", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    /**
     * Busca categorias por nome.
     * 
     * @param nome Nome da categoria para busca (busca parcial)
     * @return Lista de CategoriaDTO que correspondem ao nome
     */
    @Operation(
        summary = "Buscar categorias por nome",
        description = "Retorna uma lista de categorias que contêm o nome especificado. " +
                     "A busca é realizada de forma parcial (like)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categorias encontradas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoriaDTO.class)
            )
        )
    })
    @GetMapping("/buscar")
    public List<CategoriaDTO> buscarPorNome(
            @Parameter(description = "Nome da categoria para busca", required = true)
            @RequestParam String nome) {
        return categoriaService.buscarPorNome(nome);
    }

    /**
     * Cria uma nova categoria no sistema.
     * 
     * @param categoriaDTO Dados da categoria a ser criada
     * @return ResponseEntity contendo a CategoriaDTO da categoria criada
     */
    @Operation(
        summary = "Criar nova categoria",
        description = "Cria uma nova categoria no sistema. A categoria deve especificar " +
                     "se é do tipo receita ou despesa."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoria criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoriaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<CategoriaDTO> salvar(
            @Parameter(description = "Dados da categoria a ser criada", required = true)
            @RequestBody CategoriaDTO categoriaDTO) {
        return ResponseEntity.ok(categoriaService.salvar(categoriaDTO));
    }

    /**
     * Atualiza os dados de uma categoria existente.
     * 
     * @param id ID da categoria a ser atualizada
     * @param categoriaDTO Novos dados da categoria
     * @return ResponseEntity contendo a CategoriaDTO atualizada
     */
    @Operation(
        summary = "Atualizar categoria",
        description = "Atualiza os dados de uma categoria existente no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoria atualizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoriaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoria não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> atualizar(
            @Parameter(description = "ID da categoria a ser atualizada", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novos dados da categoria", required = true)
            @RequestBody CategoriaDTO categoriaDTO) {
        categoriaDTO.setId(id);
        return ResponseEntity.ok(categoriaService.salvar(categoriaDTO));
    }

    /**
     * Remove uma categoria do sistema.
     * 
     * @param id ID da categoria a ser removida
     * @return ResponseEntity vazio confirmando a exclusão
     */
    @Operation(
        summary = "Excluir categoria",
        description = "Remove uma categoria do sistema baseada no seu ID. " +
                     "Cuidado: esta operação pode afetar receitas e despesas relacionadas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Categoria excluída com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoria não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Categoria não pode ser excluída pois está em uso",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da categoria a ser excluída", required = true)
            @PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista apenas as categorias do tipo receita.
     * 
     * @return Lista de CategoriaDTO contendo apenas categorias de receitas
     */
    @Operation(
        summary = "Listar categorias de receitas",
        description = "Retorna uma lista contendo apenas as categorias marcadas como tipo receita."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categorias de receitas retornadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoriaDTO.class)
            )
        )
    })
    @GetMapping("/receitas")
    public List<CategoriaDTO> listarReceitas() {
        return categoriaService.buscarPorTipoReceita(true);
    }

    /**
     * Lista apenas as categorias do tipo despesa.
     * 
     * @return Lista de CategoriaDTO contendo apenas categorias de despesas
     */
    @Operation(
        summary = "Listar categorias de despesas",
        description = "Retorna uma lista contendo apenas as categorias marcadas como tipo despesa."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categorias de despesas retornadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoriaDTO.class)
            )
        )
    })
    @GetMapping("/despesas")
    public List<CategoriaDTO> listarDespesas() {
        return categoriaService.buscarPorTipoReceita(false);
    }
} 