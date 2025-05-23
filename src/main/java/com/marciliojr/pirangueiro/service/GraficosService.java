package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.DadosGraficoDTO;
import com.marciliojr.pirangueiro.dto.DashboardFinanceiroDTO;
import com.marciliojr.pirangueiro.dto.CartaoLimiteDTO;
import com.marciliojr.pirangueiro.dto.GraficoReceitasDespesasDTO;
import com.marciliojr.pirangueiro.dto.GraficoVariacaoMensalDTO;
import com.marciliojr.pirangueiro.dto.TotalMensalDTO;
import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.repository.CartaoRepository;
import com.marciliojr.pirangueiro.repository.GraficosRepository;
import com.marciliojr.pirangueiro.repository.DespesaRepository;
import com.marciliojr.pirangueiro.repository.ReceitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public GraficoVariacaoMensalDTO buscarVariacaoMensalDespesas(Integer ano) {
        List<Double> totaisMensais = graficosRepository.buscarDespesasMensaisPorAno(ano);
        List<TotalMensalDTO> dadosMensais = new ArrayList<>();
        
        for (int i = 0; i < totaisMensais.size(); i++) {
            TotalMensalDTO totalMensal = new TotalMensalDTO();
            totalMensal.setMes(i + 1);
            totalMensal.setTotal(totaisMensais.get(i));
            dadosMensais.add(totalMensal);
        }
        
        GraficoVariacaoMensalDTO dto = new GraficoVariacaoMensalDTO();
        dto.setAno(ano);
        dto.setTotaisMensais(dadosMensais);
        
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
} 