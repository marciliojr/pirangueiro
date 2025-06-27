package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsável por operações administrativas do sistema.
 * 
 * <p>Este controller fornece endpoints para operações de administração
 * e manutenção do sistema, incluindo:</p>
 * <ul>
 *   <li>Limpeza da base de dados</li>
 *   <li>Operações de reset do sistema</li>
 *   <li>Utilitários administrativos</li>
 * </ul>
 * 
 * <p><strong>ATENÇÃO:</strong> Os endpoints deste controller podem ser
 * destrutivos e devem ser usados com extrema cautela, preferencialmente
 * apenas em ambiente de desenvolvimento.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Administração", description = "APIs para operações administrativas do sistema")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private HistoricoRepository historicoRepository;
    
    @Autowired
    private NotificacaoRepository notificacaoRepository;
    
    @Autowired
    private DespesaRepository despesaRepository;
    
    @Autowired
    private ReceitaRepository receitaRepository;
    
    @Autowired
    private LimiteGastosRepository limiteGastosRepository;
    
    @Autowired
    private ExecucaoTarefaRepository execucaoTarefaRepository;
    
    @Autowired
    private CartaoRepository cartaoRepository;
    
    @Autowired
    private ContaRepository contaRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private GraficosRepository graficosRepository;

    @Autowired
    private PensamentosRepository pensamentosRepository;

    /**
     * Limpa toda a base de dados mantendo apenas a tabela de pensamentos.
     * 
     * <p>Este endpoint remove todos os registros de todas as tabelas do sistema,
     * exceto a tabela de pensamentos. A exclusão é feita respeitando a ordem
     * das foreign keys para evitar erros de integridade referencial.</p>
     * 
     * <p><strong>ATENÇÃO:</strong> Esta operação é IRREVERSÍVEL e deve ser
     * usada apenas em ambiente de desenvolvimento ou para reset completo
     * do sistema.</p>
     * 
     * @param confirmacao Parâmetro de confirmação obrigatório (deve ser "CONFIRMAR_LIMPEZA")
     * @return ResponseEntity com o resultado da operação
     */
    @Operation(
        summary = "Limpar base de dados",
        description = "Remove todos os registros da base de dados mantendo apenas a tabela de pensamentos. " +
                     "OPERAÇÃO IRREVERSÍVEL - use apenas em desenvolvimento!"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Base de dados limpa com sucesso"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetro de confirmação inválido"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno durante a limpeza"
        )
    })
    @DeleteMapping("/limpar-base-dados")
    @Transactional
    public ResponseEntity<Map<String, Object>> limparBaseDados(
            @Parameter(description = "Confirmação obrigatória (deve ser 'CONFIRMAR_LIMPEZA')", required = true)
            @RequestParam String confirmacao) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar confirmação
            if (!"CONFIRMAR_LIMPEZA".equals(confirmacao)) {
                response.put("sucesso", false);
                response.put("mensagem", "Parâmetro de confirmação inválido. Use 'CONFIRMAR_LIMPEZA'");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Contar total antes da exclusão
            long totalGeral = historicoRepository.count() + 
                             notificacaoRepository.count() + 
                             despesaRepository.count() + 
                             receitaRepository.count() + 
                             limiteGastosRepository.count() + 
                             execucaoTarefaRepository.count() + 
                             cartaoRepository.count() + 
                             contaRepository.count() + 
                             categoriaRepository.count() + 
                             usuarioRepository.count() + 
                             graficosRepository.count();
            
            // Exclusão em ordem respeitando foreign keys
            historicoRepository.deleteAll();
            notificacaoRepository.deleteAll();
            despesaRepository.deleteAll();
            receitaRepository.deleteAll();
            limiteGastosRepository.deleteAll();
            execucaoTarefaRepository.deleteAll();
            graficosRepository.deleteAll();
            cartaoRepository.deleteAll();
            contaRepository.deleteAll();
            categoriaRepository.deleteAll();
            usuarioRepository.deleteAll();
            
            response.put("sucesso", true);
            response.put("mensagem", "Base de dados limpa com sucesso!");
            response.put("totalRegistrosRemovidos", totalGeral);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("mensagem", "Erro durante a limpeza da base de dados");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    

} 