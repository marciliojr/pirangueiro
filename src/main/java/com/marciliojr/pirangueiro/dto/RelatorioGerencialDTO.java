package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para relatório gerencial completo das finanças
 */
@Data
public class RelatorioGerencialDTO {
    
    // Metadata do relatório
    private LocalDateTime dataGeracao;
    private String versao;
    
    // Seção 1: Despesas
    private SecaoDespesas secaoDespesas;
    
    // Seção 2: Receitas  
    private SecaoReceitas secaoReceitas;
    
    // Seção 3: Saldos das Contas
    private SecaoSaldosContas secaoSaldosContas;
    
    // Seção 4: Cartões de Crédito
    private SecaoCartoes secaoCartoes;
    
    // Seção 5: Análise por Categorias
    private SecaoAnaliseCategoria secaoAnaliseCategoria;
    
    // Resumo Executivo
    private ResumoExecutivo resumoExecutivo;
    
    @Data
    public static class SecaoDespesas {
        private List<DespesaDTO> todasDespesas;
        private Double totalDespesas;
        private Integer quantidadeDespesas;
        private Double valorMedioDespesas;
    }
    
    @Data
    public static class SecaoReceitas {
        private List<ReceitaDTO> todasReceitas;
        private Double totalReceitas;
        private Integer quantidadeReceitas;
        private Double valorMedioReceitas;
    }
    
    @Data
    public static class SecaoSaldosContas {
        private List<SaldoContaDetalhado> saldosDetalhados;
        private Double saldoTotalContas;
        private Double totalReceitasContas;
        private Double totalDespesasContas;
    }
    
    @Data
    public static class SaldoContaDetalhado {
        private Long contaId;
        private String nomeConta;
        private String tipoConta;
        private Double totalReceitas;
        private Double totalDespesas;
        private Double saldo;
        private String statusSaldo; // "POSITIVO", "NEGATIVO", "NEUTRO"
    }
    
    @Data
    public static class SecaoCartoes {
        private List<CartaoDetalhado> cartoesDetalhados;
        private Double limiteTotal;
        private Double limiteUsadoTotal;
        private Double limiteDisponivelTotal;
        private Double percentualUtilizacaoGeral;
    }
    
    @Data
    public static class CartaoDetalhado {
        private Long cartaoId;
        private String nomeCartao;
        private Double limite;
        private Double limiteUsado;
        private Double limiteDisponivel;
        private Double percentualUtilizacao;
        private String statusUtilizacao; // "BAIXA", "MEDIA", "ALTA", "CRITICA"
        private List<DespesaDTO> despesasNaoPagas;
    }
    
    @Data
    public static class SecaoAnaliseCategoria {
        private List<CategoriaAnalise> analiseCategoriaDespesas;
        private List<CategoriaAnalise> analiseCategoriaReceitas;
        private CategoriaAnalise categoriaMaiorDespesa;
        private CategoriaAnalise categoriaMaiorReceita;
    }
    
    @Data
    public static class CategoriaAnalise {
        private Long categoriaId;
        private String nomeCategoria;
        private String corCategoria;
        private Double valor;
        private Double percentual;
        private Integer quantidade;
        private Double valorMedio;
        private Boolean tipoReceita;
    }
    
    @Data
    public static class ResumoExecutivo {
        private Double saldoGeral;
        private String situacaoFinanceira; // "SAUDAVEL", "ATENCAO", "CRITICA"
        private Double receitaTotal;
        private Double despesaTotal;
        private Double percentualEconomia; // (receita - despesa) / receita * 100
        private String recomendacoes;
        private Map<String, Object> indicadoresChave;
    }
} 