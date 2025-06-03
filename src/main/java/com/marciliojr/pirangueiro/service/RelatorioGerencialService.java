package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.*;
import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.model.Categoria;
import com.marciliojr.pirangueiro.model.Conta;
import com.marciliojr.pirangueiro.repository.CartaoRepository;
import com.marciliojr.pirangueiro.repository.CategoriaRepository;
import com.marciliojr.pirangueiro.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioGerencialService {

    @Autowired
    private DespesaService despesaService;

    @Autowired
    private ReceitaService receitaService;

    @Autowired
    private ContaService contaService;

    @Autowired
    private CartaoService cartaoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public RelatorioGerencialDTO gerarRelatorioCompleto() {
        return gerarRelatorioCompleto(null, null);
    }

    public RelatorioGerencialDTO gerarRelatorioCompleto(Integer mes, Integer ano) {
        RelatorioGerencialDTO relatorio = new RelatorioGerencialDTO();
        
        // Metadata
        relatorio.setDataGeracao(LocalDateTime.now());
        relatorio.setVersao("1.0");

        // Gerar cada seção com filtros de período
        relatorio.setSecaoDespesas(gerarSecaoDespesas(mes, ano));
        relatorio.setSecaoReceitas(gerarSecaoReceitas(mes, ano));
        relatorio.setSecaoSaldosContas(gerarSecaoSaldosContas(mes, ano));
        relatorio.setSecaoCartoes(gerarSecaoCartoes(mes, ano));
        relatorio.setSecaoAnaliseCategoria(gerarSecaoAnaliseCategoria(mes, ano));
        relatorio.setResumoExecutivo(gerarResumoExecutivo(relatorio));

        return relatorio;
    }

    private RelatorioGerencialDTO.SecaoDespesas gerarSecaoDespesas() {
        return gerarSecaoDespesas(null, null);
    }

    private RelatorioGerencialDTO.SecaoDespesas gerarSecaoDespesas(Integer mes, Integer ano) {
        RelatorioGerencialDTO.SecaoDespesas secao = new RelatorioGerencialDTO.SecaoDespesas();
        
        List<DespesaDTO> todasDespesas;
        if (mes != null && ano != null) {
            // Filtrar por mês e ano específicos
            todasDespesas = despesaService.buscarComFiltrosSemPaginar(null, mes, ano);
        } else if (ano != null) {
            // Filtrar apenas por ano
            todasDespesas = despesaService.buscarComFiltrosSemPaginar(null, null, ano);
        } else {
            // Sem filtro - todas as despesas
            todasDespesas = despesaService.listarTodas();
        }
        
        secao.setTodasDespesas(todasDespesas);
        secao.setQuantidadeDespesas(todasDespesas.size());
        
        Double totalDespesas = todasDespesas.stream()
                .mapToDouble(DespesaDTO::getValor)
                .sum();
        secao.setTotalDespesas(totalDespesas);
        
        Double valorMedio = todasDespesas.isEmpty() ? 0.0 : totalDespesas / todasDespesas.size();
        secao.setValorMedioDespesas(valorMedio);
        
        return secao;
    }

    private RelatorioGerencialDTO.SecaoReceitas gerarSecaoReceitas() {
        return gerarSecaoReceitas(null, null);
    }

    private RelatorioGerencialDTO.SecaoReceitas gerarSecaoReceitas(Integer mes, Integer ano) {
        RelatorioGerencialDTO.SecaoReceitas secao = new RelatorioGerencialDTO.SecaoReceitas();
        
        List<ReceitaDTO> todasReceitas;
        if (mes != null && ano != null) {
            // Filtrar por mês e ano específicos
            todasReceitas = receitaService.buscarPorMesEAno(mes, ano);
        } else if (ano != null) {
            // Filtrar apenas por ano - precisaremos filtrar manualmente
            todasReceitas = receitaService.listarTodas().stream()
                    .filter(receita -> receita.getData().getYear() == ano)
                    .collect(Collectors.toList());
        } else {
            // Sem filtro - todas as receitas
            todasReceitas = receitaService.listarTodas();
        }
        
        secao.setTodasReceitas(todasReceitas);
        secao.setQuantidadeReceitas(todasReceitas.size());
        
        Double totalReceitas = todasReceitas.stream()
                .mapToDouble(ReceitaDTO::getValor)
                .sum();
        secao.setTotalReceitas(totalReceitas);
        
        Double valorMedio = todasReceitas.isEmpty() ? 0.0 : totalReceitas / todasReceitas.size();
        secao.setValorMedioReceitas(valorMedio);
        
        return secao;
    }

    private RelatorioGerencialDTO.SecaoSaldosContas gerarSecaoSaldosContas() {
        return gerarSecaoSaldosContas(null, null);
    }

    private RelatorioGerencialDTO.SecaoSaldosContas gerarSecaoSaldosContas(Integer mes, Integer ano) {
        RelatorioGerencialDTO.SecaoSaldosContas secao = new RelatorioGerencialDTO.SecaoSaldosContas();
        
        List<ContaDTO> todasContas = contaService.listarTodas();
        List<RelatorioGerencialDTO.SaldoContaDetalhado> saldosDetalhados = new ArrayList<>();
        
        Double totalReceitasGeral = 0.0;
        Double totalDespesasGeral = 0.0;
        
        for (ContaDTO conta : todasContas) {
            // Usar o filtro de período no cálculo do saldo
            SaldoContaDTO saldoConta = contaService.calcularSaldoConta(conta.getId(), mes, ano);
            
            RelatorioGerencialDTO.SaldoContaDetalhado detalhe = new RelatorioGerencialDTO.SaldoContaDetalhado();
            detalhe.setContaId(conta.getId());
            detalhe.setNomeConta(conta.getNome());
            detalhe.setTipoConta(conta.getTipo().toString());
            detalhe.setTotalReceitas(saldoConta.getTotalReceitas());
            detalhe.setTotalDespesas(saldoConta.getTotalDespesas());
            detalhe.setSaldo(saldoConta.getSaldo());
            
            // Definir status do saldo
            if (saldoConta.getSaldo() > 0) {
                detalhe.setStatusSaldo("POSITIVO");
            } else if (saldoConta.getSaldo() < 0) {
                detalhe.setStatusSaldo("NEGATIVO");
            } else {
                detalhe.setStatusSaldo("NEUTRO");
            }
            
            saldosDetalhados.add(detalhe);
            totalReceitasGeral += saldoConta.getTotalReceitas();
            totalDespesasGeral += saldoConta.getTotalDespesas();
        }
        
        secao.setSaldosDetalhados(saldosDetalhados);
        secao.setTotalReceitasContas(totalReceitasGeral);
        secao.setTotalDespesasContas(totalDespesasGeral);
        secao.setSaldoTotalContas(totalReceitasGeral - totalDespesasGeral);
        
        return secao;
    }

    private RelatorioGerencialDTO.SecaoCartoes gerarSecaoCartoes() {
        return gerarSecaoCartoes(null, null);
    }

    private RelatorioGerencialDTO.SecaoCartoes gerarSecaoCartoes(Integer mes, Integer ano) {
        RelatorioGerencialDTO.SecaoCartoes secao = new RelatorioGerencialDTO.SecaoCartoes();
        
        List<CartaoDTO> todosCartoes = cartaoService.listarTodos();
        List<RelatorioGerencialDTO.CartaoDetalhado> cartoesDetalhados = new ArrayList<>();
        
        Double limiteTotal = 0.0;
        Double limiteUsadoTotal = 0.0;
        
        for (CartaoDTO cartao : todosCartoes) {
            RelatorioGerencialDTO.CartaoDetalhado detalhe = new RelatorioGerencialDTO.CartaoDetalhado();
            
            Double limiteUsado = cartaoService.calcularLimiteUsado(cartao.getId());
            Double limiteDisponivel = cartaoService.calcularLimiteDisponivel(cartao.getId());
            Double percentualUtilizacao = (limiteUsado / cartao.getLimite()) * 100;
            
            detalhe.setCartaoId(cartao.getId());
            detalhe.setNomeCartao(cartao.getNome());
            detalhe.setLimite(cartao.getLimite());
            detalhe.setLimiteUsado(limiteUsado);
            detalhe.setLimiteDisponivel(limiteDisponivel);
            detalhe.setPercentualUtilizacao(percentualUtilizacao);
            
            // Definir status de utilização
            if (percentualUtilizacao <= 30) {
                detalhe.setStatusUtilizacao("BAIXA");
            } else if (percentualUtilizacao <= 60) {
                detalhe.setStatusUtilizacao("MEDIA");
            } else if (percentualUtilizacao <= 80) {
                detalhe.setStatusUtilizacao("ALTA");
            } else {
                detalhe.setStatusUtilizacao("CRITICA");
            }
            
            // Buscar despesas não pagas do cartão (o campo 'pago' é usado apenas para limite)
            List<DespesaDTO> despesasNaoPagas;
            if (mes != null && ano != null) {
                // Filtrar despesas não pagas por período específico
                despesasNaoPagas = despesaService.buscarComFiltrosSemPaginar(null, mes, ano).stream()
                        .filter(despesa -> despesa.getCartao() != null && 
                                despesa.getCartao().getId().equals(cartao.getId()) && 
                                !Boolean.TRUE.equals(despesa.getPago()))
                        .collect(Collectors.toList());
            } else if (ano != null) {
                // Filtrar despesas não pagas apenas por ano
                despesasNaoPagas = despesaService.buscarComFiltrosSemPaginar(null, null, ano).stream()
                        .filter(despesa -> despesa.getCartao() != null && 
                                despesa.getCartao().getId().equals(cartao.getId()) && 
                                !Boolean.TRUE.equals(despesa.getPago()))
                        .collect(Collectors.toList());
            } else {
                // Todas as despesas não pagas
                despesasNaoPagas = despesaService.listarTodas().stream()
                        .filter(despesa -> despesa.getCartao() != null && 
                                despesa.getCartao().getId().equals(cartao.getId()) && 
                                !Boolean.TRUE.equals(despesa.getPago()))
                        .collect(Collectors.toList());
            }
            detalhe.setDespesasNaoPagas(despesasNaoPagas);
            
            cartoesDetalhados.add(detalhe);
            limiteTotal += cartao.getLimite();
            limiteUsadoTotal += limiteUsado;
        }
        
        secao.setCartoesDetalhados(cartoesDetalhados);
        secao.setLimiteTotal(limiteTotal);
        secao.setLimiteUsadoTotal(limiteUsadoTotal);
        secao.setLimiteDisponivelTotal(limiteTotal - limiteUsadoTotal);
        secao.setPercentualUtilizacaoGeral(limiteTotal > 0 ? (limiteUsadoTotal / limiteTotal) * 100 : 0.0);
        
        return secao;
    }

    private RelatorioGerencialDTO.SecaoAnaliseCategoria gerarSecaoAnaliseCategoria() {
        return gerarSecaoAnaliseCategoria(null, null);
    }

    private RelatorioGerencialDTO.SecaoAnaliseCategoria gerarSecaoAnaliseCategoria(Integer mes, Integer ano) {
        RelatorioGerencialDTO.SecaoAnaliseCategoria secao = new RelatorioGerencialDTO.SecaoAnaliseCategoria();
        
        // Buscar despesas e receitas filtradas por período
        List<DespesaDTO> todasDespesas;
        List<ReceitaDTO> todasReceitas;
        
        if (mes != null && ano != null) {
            todasDespesas = despesaService.buscarComFiltrosSemPaginar(null, mes, ano);
            todasReceitas = receitaService.buscarPorMesEAno(mes, ano);
        } else if (ano != null) {
            todasDespesas = despesaService.buscarComFiltrosSemPaginar(null, null, ano);
            todasReceitas = receitaService.listarTodas().stream()
                    .filter(receita -> receita.getData().getYear() == ano)
                    .collect(Collectors.toList());
        } else {
            todasDespesas = despesaService.listarTodas();
            todasReceitas = receitaService.listarTodas();
        }
        
        // Análise categorias de despesas
        Map<Long, List<DespesaDTO>> despesasPorCategoria = todasDespesas.stream()
                .filter(despesa -> despesa.getCategoria() != null)
                .collect(Collectors.groupingBy(despesa -> despesa.getCategoria().getId()));
        
        Double totalDespesas = todasDespesas.stream().mapToDouble(DespesaDTO::getValor).sum();
        
        List<RelatorioGerencialDTO.CategoriaAnalise> analiseCategoriaDespesas = new ArrayList<>();
        RelatorioGerencialDTO.CategoriaAnalise categoriaMaiorDespesa = null;
        Double maiorValorDespesa = 0.0;
        
        for (Map.Entry<Long, List<DespesaDTO>> entry : despesasPorCategoria.entrySet()) {
            CategoriaDTO categoria = categoriaService.buscarPorId(entry.getKey());
            List<DespesaDTO> despesasCategoria = entry.getValue();
            
            Double valorCategoria = despesasCategoria.stream().mapToDouble(DespesaDTO::getValor).sum();
            Double percentual = totalDespesas > 0 ? (valorCategoria / totalDespesas) * 100 : 0.0;
            
            RelatorioGerencialDTO.CategoriaAnalise analise = new RelatorioGerencialDTO.CategoriaAnalise();
            analise.setCategoriaId(categoria.getId());
            analise.setNomeCategoria(categoria.getNome());
            analise.setCorCategoria(categoria.getCor());
            analise.setValor(valorCategoria);
            analise.setPercentual(percentual);
            analise.setQuantidade(despesasCategoria.size());
            analise.setValorMedio(valorCategoria / despesasCategoria.size());
            analise.setTipoReceita(false);
            
            analiseCategoriaDespesas.add(analise);
            
            if (valorCategoria > maiorValorDespesa) {
                maiorValorDespesa = valorCategoria;
                categoriaMaiorDespesa = analise;
            }
        }
        
        // Análise categorias de receitas
        Map<Long, List<ReceitaDTO>> receitasPorCategoria = todasReceitas.stream()
                .filter(receita -> receita.getCategoria() != null)
                .collect(Collectors.groupingBy(receita -> receita.getCategoria().getId()));
        
        Double totalReceitas = todasReceitas.stream().mapToDouble(ReceitaDTO::getValor).sum();
        
        List<RelatorioGerencialDTO.CategoriaAnalise> analiseCategoriaReceitas = new ArrayList<>();
        RelatorioGerencialDTO.CategoriaAnalise categoriaMaiorReceita = null;
        Double maiorValorReceita = 0.0;
        
        for (Map.Entry<Long, List<ReceitaDTO>> entry : receitasPorCategoria.entrySet()) {
            CategoriaDTO categoria = categoriaService.buscarPorId(entry.getKey());
            List<ReceitaDTO> receitasCategoria = entry.getValue();
            
            Double valorCategoria = receitasCategoria.stream().mapToDouble(ReceitaDTO::getValor).sum();
            Double percentual = totalReceitas > 0 ? (valorCategoria / totalReceitas) * 100 : 0.0;
            
            RelatorioGerencialDTO.CategoriaAnalise analise = new RelatorioGerencialDTO.CategoriaAnalise();
            analise.setCategoriaId(categoria.getId());
            analise.setNomeCategoria(categoria.getNome());
            analise.setCorCategoria(categoria.getCor());
            analise.setValor(valorCategoria);
            analise.setPercentual(percentual);
            analise.setQuantidade(receitasCategoria.size());
            analise.setValorMedio(valorCategoria / receitasCategoria.size());
            analise.setTipoReceita(true);
            
            analiseCategoriaReceitas.add(analise);
            
            if (valorCategoria > maiorValorReceita) {
                maiorValorReceita = valorCategoria;
                categoriaMaiorReceita = analise;
            }
        }
        
        secao.setAnaliseCategoriaDespesas(analiseCategoriaDespesas);
        secao.setAnaliseCategoriaReceitas(analiseCategoriaReceitas);
        secao.setCategoriaMaiorDespesa(categoriaMaiorDespesa);
        secao.setCategoriaMaiorReceita(categoriaMaiorReceita);
        
        return secao;
    }

    private RelatorioGerencialDTO.ResumoExecutivo gerarResumoExecutivo(RelatorioGerencialDTO relatorio) {
        RelatorioGerencialDTO.ResumoExecutivo resumo = new RelatorioGerencialDTO.ResumoExecutivo();
        
        Double receitaTotal = relatorio.getSecaoReceitas().getTotalReceitas();
        Double despesaTotal = relatorio.getSecaoDespesas().getTotalDespesas();
        Double saldoGeral = receitaTotal - despesaTotal;
        
        resumo.setReceitaTotal(receitaTotal);
        resumo.setDespesaTotal(despesaTotal);
        resumo.setSaldoGeral(saldoGeral);
        
        // Calcular percentual de economia
        Double percentualEconomia = receitaTotal > 0 ? (saldoGeral / receitaTotal) * 100 : 0.0;
        resumo.setPercentualEconomia(percentualEconomia);
        
        // Definir situação financeira
        if (percentualEconomia >= 20) {
            resumo.setSituacaoFinanceira("SAUDAVEL");
            resumo.setRecomendacoes("Situação financeira excelente! Continue mantendo este nível de economia.");
        } else if (percentualEconomia >= 10) {
            resumo.setSituacaoFinanceira("ATENCAO");
            resumo.setRecomendacoes("Situação estável, mas há espaço para melhorias. Considere revisar gastos desnecessários.");
        } else {
            resumo.setSituacaoFinanceira("CRITICA");
            resumo.setRecomendacoes("Atenção! Gastos estão muito próximos ou superiores às receitas. Revise urgentemente seu orçamento.");
        }
        
        // Indicadores chave
        Map<String, Object> indicadores = new HashMap<>();
        indicadores.put("receitaTotal", receitaTotal);
        indicadores.put("despesaTotal", despesaTotal);
        indicadores.put("saldoGeral", saldoGeral);
        indicadores.put("percentualEconomia", percentualEconomia);
        indicadores.put("quantidadeDespesas", relatorio.getSecaoDespesas().getQuantidadeDespesas());
        indicadores.put("quantidadeReceitas", relatorio.getSecaoReceitas().getQuantidadeReceitas());
        indicadores.put("valorMedioDespesas", relatorio.getSecaoDespesas().getValorMedioDespesas());
        indicadores.put("valorMedioReceitas", relatorio.getSecaoReceitas().getValorMedioReceitas());
        indicadores.put("saldoTotalContas", relatorio.getSecaoSaldosContas().getSaldoTotalContas());
        indicadores.put("percentualUtilizacaoCartoes", relatorio.getSecaoCartoes().getPercentualUtilizacaoGeral());
        
        resumo.setIndicadoresChave(indicadores);
        
        return resumo;
    }
} 