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
        return converterParaDTO(limiteGastosRepository.save(limiteGastos));
    }

    public void excluir(Long id) {
        limiteGastosRepository.deleteById(id);
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