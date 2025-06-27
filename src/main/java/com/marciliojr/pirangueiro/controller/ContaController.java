package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.service.ContaService;
import com.marciliojr.pirangueiro.dto.ContaDTO;
import com.marciliojr.pirangueiro.dto.SaldoContaDTO;
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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/**
 * Controller responsável por gerenciar operações relacionadas às contas bancárias do sistema.
 * 
 * <p>Este controller fornece endpoints para operações CRUD (Create, Read, Update, Delete)
 * de contas bancárias, incluindo funcionalidades avançadas como:</p>
 * <ul>
 *   <li>Upload de imagens de logo das contas</li>
 *   <li>Cálculo de saldo por período</li>
 *   <li>Busca por nome</li>
 *   <li>Gerenciamento de dados bancários</li>
 * </ul>
 * 
 * <p>As contas são utilizadas para organizar receitas e despesas por instituição
 * financeira, permitindo controle detalhado do fluxo de caixa.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Contas Bancárias", description = "APIs para gerenciamento de contas bancárias")
@RestController
@RequestMapping("/api/contas")
public class ContaController {

    /**
     * Serviço responsável pela lógica de negócio relacionada às contas.
     */
    @Autowired
    private ContaService contaService;

    /**
     * Lista todas as contas cadastradas no sistema.
     * 
     * @return ResponseEntity contendo lista de todas as contas
     */
    @Operation(
        summary = "Listar todas as contas",
        description = "Retorna uma lista completa de todas as contas bancárias cadastradas no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de contas retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContaDTO.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<Object> listarTodas() {
        return ResponseEntity.ok(contaService.listarTodas());
    }

    /**
     * Busca uma conta específica pelo seu ID.
     * 
     * @param id ID único da conta a ser buscada
     * @return ResponseEntity contendo a conta se encontrada
     * @throws RuntimeException se a conta não for encontrada
     */
    @Operation(
        summary = "Buscar conta por ID",
        description = "Retorna os detalhes de uma conta específica baseada no seu ID único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Conta encontrada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Conta não encontrada",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(
            @Parameter(description = "ID único da conta", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    /**
     * Busca contas por nome.
     * 
     * @param nome Nome da conta para busca (busca parcial)
     * @return ResponseEntity contendo lista de contas que correspondem ao nome
     */
    @Operation(
        summary = "Buscar contas por nome",
        description = "Retorna uma lista de contas que contêm o nome especificado. " +
                     "A busca é realizada de forma parcial (like)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Contas encontradas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContaDTO.class)
            )
        )
    })
    @GetMapping("/buscar")
    public ResponseEntity<Object> buscarPorNome(
            @Parameter(description = "Nome da conta para busca", required = true)
            @RequestParam String nome) {
        return ResponseEntity.ok(contaService.buscarPorNome(nome));
    }

    /**
     * Cria uma nova conta no sistema.
     * 
     * <p>Este endpoint suporta upload de imagem de logo da conta como arquivo multipart.</p>
     * 
     * @param contaDTO Dados da conta a ser criada
     * @param imagemLogo Arquivo de imagem do logo da conta (opcional)
     * @return ResponseEntity contendo a conta criada
     */
    @Operation(
        summary = "Criar nova conta",
        description = "Cria uma nova conta bancária no sistema. Opcionalmente, permite " +
                     "o upload de uma imagem de logo da instituição financeira."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Conta criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<Object> salvar(
            @Parameter(description = "Dados da conta a ser criada", required = true)
            @RequestPart("conta") ContaDTO contaDTO,
            @Parameter(description = "Arquivo de imagem do logo da conta")
            @RequestPart(value = "imagemLogo", required = false) MultipartFile imagemLogo) {
        return ResponseEntity.ok(contaService.salvar(contaDTO, imagemLogo));
    }

    /**
     * Atualiza os dados de uma conta existente.
     * 
     * <p>Este endpoint suporta atualização da imagem de logo da conta.</p>
     * 
     * @param id ID da conta a ser atualizada
     * @param contaDTO Novos dados da conta
     * @param imagemLogo Nova imagem de logo da conta (opcional)
     * @return ResponseEntity contendo a conta atualizada
     */
    @Operation(
        summary = "Atualizar conta",
        description = "Atualiza os dados de uma conta existente no sistema. " +
                     "Permite também atualizar a imagem de logo da conta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Conta atualizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Conta não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content
        )
    })
    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<ContaDTO> atualizar(
            @Parameter(description = "ID da conta a ser atualizada", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novos dados da conta", required = true)
            @RequestPart("conta") ContaDTO contaDTO,
            @Parameter(description = "Nova imagem de logo da conta")
            @RequestPart(value = "imagemLogo", required = false) MultipartFile imagemLogo) {
        contaDTO.setId(id);
        return ResponseEntity.ok(contaService.salvar(contaDTO, imagemLogo));
    }

    /**
     * Remove uma conta do sistema.
     * 
     * @param id ID da conta a ser removida
     * @return ResponseEntity vazio confirmando a exclusão
     */
    @Operation(
        summary = "Excluir conta",
        description = "Remove uma conta do sistema baseada no seu ID. " +
                     "Cuidado: esta operação pode afetar receitas e despesas relacionadas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Conta excluída com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Conta não encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conta não pode ser excluída pois está em uso",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da conta a ser excluída", required = true)
            @PathVariable Long id) {
        contaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calcula o saldo de uma conta específica.
     * 
     * @param id ID da conta para cálculo do saldo
     * @param mes Mês opcional para filtro (1-12)
     * @param ano Ano opcional para filtro
     * @return ResponseEntity contendo o SaldoContaDTO
     */
    @Operation(
        summary = "Calcular saldo da conta",
        description = "Calcula o saldo de uma conta específica, considerando receitas e despesas. " +
                     "Pode ser filtrado por mês e ano específicos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Saldo calculado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SaldoContaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Conta não encontrada",
            content = @Content
        )
    })
    @GetMapping("/{id}/saldo")
    public ResponseEntity<SaldoContaDTO> calcularSaldo(
            @Parameter(description = "ID da conta para cálculo do saldo", required = true)
            @PathVariable Long id,
            @Parameter(description = "Mês para filtro (1-12)")
            @RequestParam(required = false) Integer mes,
            @Parameter(description = "Ano para filtro")
            @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(contaService.calcularSaldoConta(id, mes, ano));
    }
} 