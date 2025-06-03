package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.LimiteGastos;
import com.marciliojr.pirangueiro.repository.LimiteGastosRepository;
import com.marciliojr.pirangueiro.dto.LimiteGastosDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LimiteGastosService {

    @Autowired
    private LimiteGastosRepository limiteGastosRepository;

    @Autowired
    private HistoricoService historicoService;

    public List<LimiteGastosDTO> listarTodos() {
        return limiteGastosRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public LimiteGastosDTO buscarPorId(Long id) {
        return limiteGastosRepository.findById(id)
                .map(this::converterParaDTO)
                .orElseThrow(() -> new RuntimeException("Limite de gastos não encontrado"));
    }

    public List<LimiteGastosDTO> buscarPorDescricao(String descricao) {
        return limiteGastosRepository.findByDescricaoContainingIgnoreCase(descricao).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public LimiteGastosDTO salvar(LimiteGastosDTO limiteGastosDTO) {
        LimiteGastos limiteGastos = converterParaEntidade(limiteGastosDTO);
        LimiteGastos salvos = limiteGastosRepository.save(limiteGastos);
        
        // Registrar no histórico
        try {
            if (limiteGastosDTO.getId() == null) {
                // Criação
                historicoService.registrarCriacaoLimiteGastos(salvos.getId(), salvos.toString(), null);
            } else {
                // Edição
                historicoService.registrarEdicaoLimiteGastos(salvos.getId(), salvos.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
        
        return converterParaDTO(salvos);
    }

    public void excluir(Long id) {
        try {
            // Buscar o limite de gastos antes de excluir para registrar no histórico
            LimiteGastos limiteGastos = limiteGastosRepository.findById(id).orElse(null);
            
            limiteGastosRepository.deleteById(id);
            
            // Registrar exclusão no histórico
            if (limiteGastos != null) {
                historicoService.registrarExclusaoLimiteGastos(id, limiteGastos.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao excluir limite de gastos ou registrar histórico: " + e.getMessage());
            throw e;
        }
    }

    private LimiteGastosDTO converterParaDTO(LimiteGastos limiteGastos) {
        LimiteGastosDTO dto = new LimiteGastosDTO();
        dto.setId(limiteGastos.getId());
        dto.setDescricao(limiteGastos.getDescricao());
        dto.setValor(limiteGastos.getValor());
        dto.setData(limiteGastos.getData());
        return dto;
    }

    private LimiteGastos converterParaEntidade(LimiteGastosDTO dto) {
        LimiteGastos limiteGastos = new LimiteGastos();
        limiteGastos.setId(dto.getId());
        limiteGastos.setDescricao(dto.getDescricao());
        limiteGastos.setValor(dto.getValor());
        limiteGastos.setData(dto.getData());
        return limiteGastos;
    }
} 