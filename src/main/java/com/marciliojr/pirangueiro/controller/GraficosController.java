package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.DashboardFinanceiroDTO;
import com.marciliojr.pirangueiro.dto.GraficoReceitasDespesasCategoriaDTO;
import com.marciliojr.pirangueiro.dto.GraficoDespesasCartaoDTO;
import com.marciliojr.pirangueiro.dto.GraficoSazonalidadeGastosDTO;
import com.marciliojr.pirangueiro.dto.GraficoTendenciaGastosDTO;
import com.marciliojr.pirangueiro.dto.GraficoReceitasDespesasResponseDTO;
import com.marciliojr.pirangueiro.service.GraficosService;
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

import java.time.LocalDate;

/**
 * Controller responsável por fornecer dados para gráficos e dashboards financeiros.
 * 
 * <p>Este controller oferece endpoints especializados para geração de dados analíticos
 * e visualizações financeiras, incluindo:</p>
 * <ul>
 *   <li>Dashboard financeiro com métricas consolidadas</li>
 *   <li>Gráficos de receitas vs despesas por categoria</li>
 *   <li>Análise de despesas por cartão ao longo do tempo</li>
 *   <li>Análise de sazonalidade de gastos</li>
 *   <li>Tendências de gastos</li>
 *   <li>Comparativo de receitas e despesas por período</li>
 * </ul>
 * 
 * <p>Todos os endpoints são projetados para fornecer dados prontos para
 * consumo por componentes de visualização frontend.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Gráficos e Analytics", description = "APIs para dados de gráficos e análises financeiras")
@RestController
@RequestMapping("/api/graficos")
@CrossOrigin(origins = "*")
public class GraficosController {

    /**
     * Serviço responsável pela lógica de negócio dos gráficos e análises.
     */
    @Autowired
    private GraficosService graficosService;

    /**
     * Busca dados para gráfico de receitas vs despesas por categoria.
     * 
     * @param mes Mês para análise (1-12)
     * @param ano Ano para análise
     * @return ResponseEntity contendo dados agrupados por categoria
     */
    @Operation(
        summary = "Gráfico de receitas vs despesas por categoria",
        description = "Retorna dados agregados de receitas e despesas organizados por categoria " +
                     "para um mês e ano específicos, prontos para visualização em gráficos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados do gráfico retornados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GraficoReceitasDespesasCategoriaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros inválidos fornecidos",
            content = @Content
        )
    })
    @GetMapping("/receitas-despesas-categoria")
    public ResponseEntity<GraficoReceitasDespesasCategoriaDTO> buscarGraficoReceitasDespesas(
            @Parameter(description = "Mês para análise (1-12)", required = true)
            @RequestParam Integer mes,
            @Parameter(description = "Ano para análise", required = true)
            @RequestParam Integer ano) {
        return ResponseEntity.ok(graficosService.buscarDadosGraficoReceitasDespesasCategoria(mes, ano));
    }

    /**
     * Busca dados consolidados para o dashboard financeiro.
     * 
     * @param mes Mês para análise (1-12)
     * @param ano Ano para análise
     * @return ResponseEntity contendo métricas financeiras consolidadas
     */
    @Operation(
        summary = "Dashboard financeiro",
        description = "Retorna métricas financeiras consolidadas para exibição em dashboard, " +
                     "incluindo totais, comparativos e indicadores principais."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados do dashboard retornados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardFinanceiroDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros inválidos fornecidos",
            content = @Content
        )
    })
    @GetMapping("/dashboard-financeiro")
    public ResponseEntity<DashboardFinanceiroDTO> getDashboardFinanceiro(
            @Parameter(description = "Mês para análise (1-12)", required = true)
            @RequestParam Integer mes,
            @Parameter(description = "Ano para análise", required = true)
            @RequestParam Integer ano) {
        return ResponseEntity.ok(graficosService.getDashboardFinanceiro(mes, ano));
    }

    /**
     * Busca dados de despesas por cartão ao longo do tempo.
     * 
     * @param mesesAtras Número de meses anteriores para análise (padrão: 12)
     * @return ResponseEntity contendo evolução das despesas por cartão
     */
    @Operation(
        summary = "Gráfico de despesas por cartão ao longo do tempo",
        description = "Retorna a evolução das despesas de cada cartão de crédito ao longo " +
                     "de um período especificado, útil para análise de uso dos cartões."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados das despesas por cartão retornados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GraficoDespesasCartaoDTO.class)
            )
        )
    })
    @GetMapping("/despesas-por-cartao")
    public ResponseEntity<GraficoDespesasCartaoDTO> buscarDespesasPorCartao(
            @Parameter(description = "Número de meses anteriores para análise")
            @RequestParam(defaultValue = "12") Integer mesesAtras) {
        return ResponseEntity.ok(graficosService.buscarDespesasPorCartaoAoLongoDoTempo(mesesAtras));
    }

    /**
     * Busca dados de sazonalidade dos gastos.
     * 
     * @return ResponseEntity contendo padrões sazonais de gastos
     */
    @Operation(
        summary = "Análise de sazonalidade dos gastos",
        description = "Retorna análise dos padrões sazonais de gastos, identificando " +
                     "períodos de maior e menor movimentação financeira ao longo do ano."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados de sazonalidade retornados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GraficoSazonalidadeGastosDTO.class)
            )
        )
    })
    @GetMapping("/sazonalidade-gastos")
    public ResponseEntity<GraficoSazonalidadeGastosDTO> buscarSazonalidadeGastos() {
        return ResponseEntity.ok(graficosService.buscarSazonalidadeGastos());
    }

    /**
     * Busca dados de tendência dos gastos.
     * 
     * @return ResponseEntity contendo análise de tendências dos gastos
     */
    @Operation(
        summary = "Análise de tendência dos gastos",
        description = "Retorna análise das tendências de gastos ao longo do tempo, " +
                     "identificando padrões de crescimento, redução ou estabilidade."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados de tendência retornados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GraficoTendenciaGastosDTO.class)
            )
        )
    })
    @GetMapping("/tendencia-gastos")
    public ResponseEntity<GraficoTendenciaGastosDTO> buscarTendenciaGastos() {
        return ResponseEntity.ok(graficosService.buscarTendenciaGastos());
    }

    /**
     * Busca dados de receitas vs despesas por período customizado.
     * 
     * @param dataInicio Data de início do período (opcional)
     * @param dataFim Data de fim do período (opcional)
     * @return ResponseEntity contendo comparativo de receitas e despesas
     */
    @Operation(
        summary = "Gráfico de receitas vs despesas por período",
        description = "Retorna comparativo entre receitas e despesas para um período customizado. " +
                     "Se as datas não forem informadas, utiliza um período padrão."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados comparativos retornados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GraficoReceitasDespesasResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Período inválido ou parâmetros incorretos",
            content = @Content
        )
    })
    @GetMapping("/receitas-despesas")
    public ResponseEntity<GraficoReceitasDespesasResponseDTO> buscarGraficoReceitasDespesasPorMes(
            @Parameter(description = "Data de início do período (formato: YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data de fim do período (formato: YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        try {
            GraficoReceitasDespesasResponseDTO resultado = 
                graficosService.buscarGraficoReceitasDespesasPorMes(dataInicio, dataFim);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 