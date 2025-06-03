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
        Map<String, Long> registrosRemovidos = new HashMap<>();
        
        try {
            // Validar confirmação
            if (!"CONFIRMAR_LIMPEZA".equals(confirmacao)) {
                response.put("sucesso", false);
                response.put("mensagem", "Parâmetro de confirmação inválido. Use 'CONFIRMAR_LIMPEZA'");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Contar registros antes da exclusão
            long totalHistorico = historicoRepository.count();
            long totalNotificacoes = notificacaoRepository.count();
            long totalDespesas = despesaRepository.count();
            long totalReceitas = receitaRepository.count();
            long totalLimiteGastos = limiteGastosRepository.count();
            long totalExecucaoTarefa = execucaoTarefaRepository.count();
            long totalCartoes = cartaoRepository.count();
            long totalContas = contaRepository.count();
            long totalCategorias = categoriaRepository.count();
            long totalUsuarios = usuarioRepository.count();
            long totalGraficos = graficosRepository.count();
            long totalPensamentos = pensamentosRepository.count(); // Conta mas não remove
            
            // Exclusão em ordem respeitando foreign keys
            
            // 1. Histórico (referencia Usuario)
            historicoRepository.deleteAll();
            registrosRemovidos.put("historico", totalHistorico);
            
            // 2. Notificações (referencia Cartao)
            notificacaoRepository.deleteAll();
            registrosRemovidos.put("notificacoes", totalNotificacoes);
            
            // 3. Despesas (referencia Conta, Cartao, Categoria)
            despesaRepository.deleteAll();
            registrosRemovidos.put("despesas", totalDespesas);
            
            // 4. Receitas (referencia Conta, Categoria)
            receitaRepository.deleteAll();
            registrosRemovidos.put("receitas", totalReceitas);
            
            // 5. Limite de Gastos
            limiteGastosRepository.deleteAll();
            registrosRemovidos.put("limiteGastos", totalLimiteGastos);
            
            // 6. Execução de Tarefas
            execucaoTarefaRepository.deleteAll();
            registrosRemovidos.put("execucaoTarefas", totalExecucaoTarefa);
            
            // 7. Gráficos
            graficosRepository.deleteAll();
            registrosRemovidos.put("graficos", totalGraficos);
            
            // 8. Cartões
            cartaoRepository.deleteAll();
            registrosRemovidos.put("cartoes", totalCartoes);
            
            // 9. Contas
            contaRepository.deleteAll();
            registrosRemovidos.put("contas", totalContas);
            
            // 10. Categorias
            categoriaRepository.deleteAll();
            registrosRemovidos.put("categorias", totalCategorias);
            
            // 11. Usuários
            usuarioRepository.deleteAll();
            registrosRemovidos.put("usuarios", totalUsuarios);
            
            // PENSAMENTOS NÃO SÃO REMOVIDOS - apenas contados
            registrosRemovidos.put("pensamentosMANTIDOS", totalPensamentos);
            
            // Calcular total de registros removidos (excluindo pensamentos)
            long totalRemovido = registrosRemovidos.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("pensamentosMANTIDOS"))
                    .mapToLong(Map.Entry::getValue)
                    .sum();
            
            response.put("sucesso", true);
            response.put("mensagem", "Base de dados limpa com sucesso! Tabela de pensamentos mantida.");
            response.put("totalRegistrosRemovidos", totalRemovido);
            response.put("totalPensamentosMantidos", totalPensamentos);
            response.put("detalhePorTabela", registrosRemovidos);
            response.put("tabelasMantidas", new String[]{"pensamentos"});
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("mensagem", "Erro durante a limpeza da base de dados: " + e.getMessage());
            response.put("erro", e.getClass().getSimpleName());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Retorna informações sobre o estado atual da base de dados.
     * 
     * @return ResponseEntity com contadores de registros por tabela
     */
    @Operation(
        summary = "Status da base de dados",
        description = "Retorna informações sobre a quantidade de registros em cada tabela"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status retornado com sucesso"
        )
    })
    @GetMapping("/status-base-dados")
    public ResponseEntity<Map<String, Object>> statusBaseDados() {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> contadores = new HashMap<>();
        
        try {
            contadores.put("historico", historicoRepository.count());
            contadores.put("notificacoes", notificacaoRepository.count());
            contadores.put("despesas", despesaRepository.count());
            contadores.put("receitas", receitaRepository.count());
            contadores.put("limiteGastos", limiteGastosRepository.count());
            contadores.put("execucaoTarefas", execucaoTarefaRepository.count());
            contadores.put("cartoes", cartaoRepository.count());
            contadores.put("contas", contaRepository.count());
            contadores.put("categorias", categoriaRepository.count());
            contadores.put("usuarios", usuarioRepository.count());
            contadores.put("graficos", graficosRepository.count());
            contadores.put("pensamentos", pensamentosRepository.count());
            
            long totalRegistros = contadores.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            
            response.put("sucesso", true);
            response.put("totalRegistros", totalRegistros);
            response.put("contadorPorTabela", contadores);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao obter status da base de dados: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 