package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.DadosGraficoDTO;
import com.marciliojr.pirangueiro.dto.GraficoReceitasDespesasDTO;
import com.marciliojr.pirangueiro.dto.GraficoVariacaoMensalDTO;
import com.marciliojr.pirangueiro.dto.TotalMensalDTO;
import com.marciliojr.pirangueiro.repository.GraficosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GraficosService {

    @Autowired
    private GraficosRepository graficosRepository;

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
} 