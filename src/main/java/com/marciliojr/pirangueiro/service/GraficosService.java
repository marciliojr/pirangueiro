package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.*;
import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.repository.CartaoRepository;
import com.marciliojr.pirangueiro.repository.GraficosRepository;
import com.marciliojr.pirangueiro.repository.DespesaRepository;
import com.marciliojr.pirangueiro.repository.ReceitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class GraficosService {

    @Autowired
    private GraficosRepository graficosRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    public GraficoReceitasDespesasDTO buscarDadosGraficoReceitasDespesas(Integer mes, Integer ano) {
        List<Object[]> dadosReceitas = graficosRepository.buscarReceitasPorCategoriaMesAno(mes, ano);
        List<Object[]> dadosDespesas = graficosRepository.buscarDespesasPorCategoriaMesAno(mes, ano);

        Double totalReceitas = graficosRepository.buscarTotalReceitasPorMesAno(mes, ano);
        Double totalDespesas = graficosRepository.buscarTotalDespesasPorMesAno(mes, ano);

        List<DadosGraficoDTO> receitas = converterParaDadosGrafico(dadosReceitas, totalReceitas);
        List<DadosGraficoDTO> despesas = converterParaDadosGrafico(dadosDespesas, totalDespesas);

        GraficoReceitasDespesasDTO dto = new GraficoReceitasDespesasDTO();
        dto.setMes(mes);
        dto.setAno(ano);
        dto.setReceitas(receitas);
        dto.setDespesas(despesas);
        dto.setTotalReceitas(totalReceitas);
        dto.setTotalDespesas(totalDespesas);
        dto.setSaldo(totalReceitas - totalDespesas);

        return dto;
    }


    private List<DadosGraficoDTO> converterParaDadosGrafico(List<Object[]> dados, Double total) {
        List<DadosGraficoDTO> resultado = new ArrayList<>();

        for (Object[] dado : dados) {
            Map<String, Object> map = (Map<String, Object>) dado[0];
            String categoria = (String) map.get("categoria");
            Double valor = (Double) map.get("valor");

            DadosGraficoDTO dadosGrafico = new DadosGraficoDTO();
            dadosGrafico.setCategoria(categoria);
            dadosGrafico.setValor(valor);
            dadosGrafico.setPercentual((valor / total) * 100);

            resultado.add(dadosGrafico);
        }

        return resultado;
    }

    public DashboardFinanceiroDTO getDashboardFinanceiro(Integer mes, Integer ano) {
        DashboardFinanceiroDTO dashboard = new DashboardFinanceiroDTO();

        // Calcula o saldo atual (receitas - despesas)
        Double totalReceitas = receitaRepository.buscarTotalReceitasPorMesAno(mes, ano);
        Double totalDespesas = despesaRepository.buscarTotalDespesasPorMesAno(mes, ano);
        Double saldoAtual = totalReceitas - totalDespesas;
        dashboard.setSaldoAtual(saldoAtual);

        // Calcula a taxa de economia mensal ((receitas - despesas) / receitas * 100)
        Double taxaEconomia = totalReceitas > 0 ? ((totalReceitas - totalDespesas) / totalReceitas * 100) : 0.0;
        dashboard.setTaxaEconomiaMensal(taxaEconomia);

        // Busca informações de limite dos cartões
        List<CartaoLimiteDTO> limitesCartoes = new ArrayList<>();
        List<Cartao> cartoes = cartaoRepository.findAll();

        for (Cartao cartao : cartoes) {
            CartaoLimiteDTO limiteDTO = new CartaoLimiteDTO();
            limiteDTO.setNomeCartao(cartao.getNome());
            limiteDTO.setLimiteTotal(cartao.getLimite());

            // Calcula o limite usado (soma das despesas não pagas do cartão)
            Double limiteUsado = cartaoRepository.calcularTotalDespesasPorCartao(cartao.getId());
            limiteDTO.setLimiteUsado(limiteUsado);

            // Calcula o limite disponível
            Double limiteDisponivel = cartao.getLimite() - limiteUsado;
            limiteDTO.setLimiteDisponivel(limiteDisponivel);

            // Calcula o percentual utilizado
            Double percentualUtilizado = (limiteUsado / cartao.getLimite()) * 100;
            limiteDTO.setPercentualUtilizado(percentualUtilizado);

            limitesCartoes.add(limiteDTO);
        }

        dashboard.setLimitesCartoes(limitesCartoes);

        return dashboard;
    }

    public GraficoSazonalidadeGastosDTO buscarSazonalidadeGastos() {
        // Busca os dados do banco
        List<Object[]> dadosMedias = graficosRepository.buscarMediaHistoricaGastosPorMes();

        // Prepara a estrutura do DTO
        GraficoSazonalidadeGastosDTO dto = new GraficoSazonalidadeGastosDTO();

        // Inicializa as listas
        List<String> meses = new ArrayList<>();
        List<Double> mediasGastos = new ArrayList<>();

        // Variáveis para controlar máximos e mínimos
        double maiorMedia = Double.MIN_VALUE;
        double menorMedia = Double.MAX_VALUE;
        String mesMaiorGasto = "";
        String mesMenorGasto = "";

        // Processa os dados
        for (Object[] dado : dadosMedias) {
            Map<String, Object> map = (Map<String, Object>) dado[0];
            Integer mesNumero = (Integer) map.get("mes");
            Double mediaGastos = (Double) map.get("mediaGastos");

            // Obtém o nome do mês em português
            String nomeMes = Month.of(mesNumero)
                    .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))
                    .toUpperCase();

            meses.add(nomeMes);
            mediasGastos.add(mediaGastos);

            // Atualiza máximos e mínimos
            if (mediaGastos > maiorMedia) {
                maiorMedia = mediaGastos;
                mesMaiorGasto = nomeMes;
            }
            if (mediaGastos < menorMedia) {
                menorMedia = mediaGastos;
                mesMenorGasto = nomeMes;
            }
        }

        // Configura o DTO
        dto.setMeses(meses);
        dto.setMediasGastos(mediasGastos);
        dto.setMaiorMedia(maiorMedia);
        dto.setMenorMedia(menorMedia);
        dto.setMesMaiorGasto(mesMaiorGasto);
        dto.setMesMenorGasto(mesMenorGasto);

        return dto;
    }

    public GraficoDespesasCartaoDTO buscarDespesasPorCartaoAoLongoDoTempo(Integer mesesFiltro) {

        LocalDate dataFim;
        LocalDate dataInicio;

        if (mesesFiltro == null || mesesFiltro >= 18) {
            dataFim = LocalDate.now().plusMonths(mesesFiltro);
            dataInicio = LocalDate.now().minusMonths(mesesFiltro);
        } else {
            dataFim = LocalDate.now();
            dataInicio = LocalDate.now().minusMonths(mesesFiltro);
        }

        // Busca os dados do banco
        List<Object[]> dadosDespesas = graficosRepository.buscarDespesasPorCartaoNoPeriodo(dataInicio, dataFim);

        // Prepara a estrutura do DTO
        GraficoDespesasCartaoDTO dto = new GraficoDespesasCartaoDTO();

        // Gera a lista de meses para o período
        List<String> meses = new ArrayList<>();
        YearMonth mesAtual = YearMonth.from(dataInicio);
        while (!mesAtual.isAfter(YearMonth.from(dataFim))) {
            meses.add(mesAtual.getMonthValue() + "/" + mesAtual.getYear());
            mesAtual = mesAtual.plusMonths(1);
        }
        dto.setMeses(meses);

        // Organiza os dados por cartão
        Map<String, Map<String, Double>> dadosPorCartao = new HashMap<>();
        Double valorTotalPeriodo = 0.0;

        for (Object[] dado : dadosDespesas) {
            Map<String, Object> map = (Map<String, Object>) dado[0];
            String mes = (String) map.get("mes");
            String cartao = (String) map.get("cartao");
            Double valor = (Double) map.get("valor");

            dadosPorCartao.computeIfAbsent(cartao, k -> new HashMap<>());
            dadosPorCartao.get(cartao).put(mes, valor);
            valorTotalPeriodo += valor;
        }

        // Converte os dados para o formato das séries
        List<GraficoDespesasCartaoDTO.SerieCartaoDTO> series = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : dadosPorCartao.entrySet()) {
            GraficoDespesasCartaoDTO.SerieCartaoDTO serie = new GraficoDespesasCartaoDTO.SerieCartaoDTO();
            serie.setNomeCartao(entry.getKey());

            // Preenche os valores mensais, usando 0.0 para meses sem despesas
            List<Double> valores = meses.stream()
                    .map(mes -> entry.getValue().getOrDefault(mes, 0.0))
                    .collect(Collectors.toList());
            serie.setValores(valores);

            // Calcula o total do cartão
            Double totalCartao = entry.getValue().values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
            serie.setValorTotal(totalCartao);

            series.add(serie);
        }

        dto.setSeries(series);
        dto.setValorTotalPeriodo(valorTotalPeriodo);

        return dto;
    }

    public GraficoTendenciaGastosDTO buscarTendenciaGastos() {
        // Calcula o período de análise (últimos 12 meses)
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusMonths(11); // Para incluir o mês atual

        // Busca os dados do banco
        List<Object[]> dadosDespesas = graficosRepository.buscarDespesasUltimos12Meses(dataInicio, dataFim);

        // Prepara o DTO
        GraficoTendenciaGastosDTO dto = new GraficoTendenciaGastosDTO();
        List<String> meses = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        // Processa os dados
        for (Object[] dado : dadosDespesas) {
            Map<String, Object> map = (Map<String, Object>) dado[0];
            String mes = (String) map.get("mes");
            Double valor = (Double) map.get("valor");

            meses.add(mes);
            valores.add(valor);
        }

        // Calcula a média dos gastos
        double mediaGastos = valores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Calcula a regressão linear
        double[] indices = IntStream.range(0, valores.size())
                .mapToDouble(i -> i)
                .toArray();

        double mediaIndices = Arrays.stream(indices).average().orElse(0.0);
        double mediaValores = valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Calcula o coeficiente angular (slope)
        double numerador = 0.0;
        double denominador = 0.0;

        for (int i = 0; i < valores.size(); i++) {
            numerador += (indices[i] - mediaIndices) * (valores.get(i) - mediaValores);
            denominador += Math.pow(indices[i] - mediaIndices, 2);
        }

        double coeficienteAngular = denominador != 0 ? numerador / denominador : 0.0;

        // Determina a tendência
        String tendencia;
        if (Math.abs(coeficienteAngular) < mediaGastos * 0.05) { // 5% da média como threshold
            tendencia = "ESTÁVEL";
        } else {
            tendencia = coeficienteAngular > 0 ? "CRESCENTE" : "DECRESCENTE";
        }

        // Calcula a previsão para o próximo mês
        double valorPrevistoProximoMes = mediaValores + coeficienteAngular * valores.size();

        // Configura o DTO
        dto.setMeses(meses);
        dto.setValores(valores);
        dto.setCoeficienteAngular(coeficienteAngular);
        dto.setMediaGastos(mediaGastos);
        dto.setTendencia(tendencia);
        dto.setValorPrevistoProximoMes(valorPrevistoProximoMes);

        return dto;
    }

} 