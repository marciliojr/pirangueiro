package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.UsuarioDTO;
import com.marciliojr.pirangueiro.model.Usuario;
import com.marciliojr.pirangueiro.service.UsuarioService;
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
import java.util.stream.Collectors;

/**
 * Controller responsável por gerenciar operações relacionadas aos usuários do sistema.
 * 
 * <p>Este controller fornece endpoints para operações CRUD (Create, Read, Update, Delete)
 * de usuários, incluindo funcionalidades de busca por ID, nome e verificação de existência.</p>
 * 
 * <p>Todos os endpoints retornam dados no formato DTO (Data Transfer Object) para 
 * garantir segurança e controle sobre os dados expostos pela API.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Usuários", description = "APIs para gerenciamento de usuários do sistema")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    /**
     * Serviço responsável pela lógica de negócio relacionada aos usuários.
     */
    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista todos os usuários cadastrados no sistema.
     * 
     * @return Lista de UsuarioDTO contendo todos os usuários
     */
    @Operation(
        summary = "Listar todos os usuários",
        description = "Retorna uma lista completa de todos os usuários cadastrados no sistema. " +
                     "Por motivos de segurança, as senhas não são incluídas na resposta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UsuarioDTO.class)
            )
        )
    })
    @GetMapping
    public List<UsuarioDTO> listarTodos() {
        return usuarioService.listarTodos().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca um usuário específico pelo seu ID.
     * 
     * @param id ID único do usuário a ser buscado
     * @return ResponseEntity contendo o UsuarioDTO se encontrado
     * @throws RuntimeException se o usuário não for encontrado
     */
    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna os detalhes de um usuário específico baseado no seu ID único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário encontrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UsuarioDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(
            @Parameter(description = "ID único do usuário", required = true)
            @PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(convertToDTO(usuario));
    }

    /**
     * Cria um novo usuário no sistema.
     * 
     * @param usuarioDTO Dados do usuário a ser criado
     * @return ResponseEntity contendo o UsuarioDTO do usuário criado
     */
    @Operation(
        summary = "Criar novo usuário",
        description = "Cria um novo usuário no sistema com os dados fornecidos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UsuarioDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<UsuarioDTO> salvar(
            @Parameter(description = "Dados do usuário a ser criado", required = true)
            @RequestBody UsuarioDTO usuarioDTO) {
        Usuario usuario = convertToEntity(usuarioDTO);
        usuario = usuarioService.salvar(usuario);
        return ResponseEntity.ok(convertToDTO(usuario));
    }

    /**
     * Atualiza os dados de um usuário existente.
     * 
     * @param id ID do usuário a ser atualizado
     * @param usuarioDTO Novos dados do usuário
     * @return ResponseEntity contendo o UsuarioDTO atualizado
     */
    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza os dados de um usuário existente no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UsuarioDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(
            @Parameter(description = "ID do usuário a ser atualizado", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novos dados do usuário", required = true)
            @RequestBody UsuarioDTO usuarioDTO) {
        usuarioDTO.setId(id);
        Usuario usuario = convertToEntity(usuarioDTO);
        usuario = usuarioService.salvar(usuario);
        return ResponseEntity.ok(convertToDTO(usuario));
    }

    /**
     * Remove um usuário do sistema.
     * 
     * @param id ID do usuário a ser removido
     * @return ResponseEntity vazio confirmando a exclusão
     */
    @Operation(
        summary = "Excluir usuário",
        description = "Remove um usuário do sistema baseado no seu ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Usuário excluído com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do usuário a ser excluído", required = true)
            @PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica se existe um usuário com o nome especificado.
     * 
     * @param nome Nome do usuário a ser verificado
     * @return ResponseEntity contendo true se o usuário existir, false caso contrário
     */
    @Operation(
        summary = "Verificar existência de usuário por nome",
        description = "Verifica se já existe um usuário cadastrado com o nome especificado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verificação realizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)
            )
        )
    })
    @GetMapping("/existe/{nome}")
    public ResponseEntity<Boolean> existePorNome(
            @Parameter(description = "Nome do usuário a ser verificado", required = true)
            @PathVariable String nome) {
        return ResponseEntity.ok(usuarioService.existePorNome(nome));
    }

    /**
     * Converte uma entidade Usuario para UsuarioDTO.
     * 
     * <p>Por questões de segurança, a senha não é incluída no DTO de resposta.</p>
     * 
     * @param usuario Entidade Usuario a ser convertida
     * @return UsuarioDTO correspondente
     */
    private UsuarioDTO convertToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        // Não retornar a senha no DTO por segurança
        return dto;
    }

    /**
     * Converte um UsuarioDTO para entidade Usuario.
     * 
     * @param dto UsuarioDTO a ser convertido
     * @return Entidade Usuario correspondente
     */
    private Usuario convertToEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNome(dto.getNome());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }
} 