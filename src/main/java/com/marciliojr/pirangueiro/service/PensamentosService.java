package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Pensamentos;
import com.marciliojr.pirangueiro.repository.PensamentosRepository;
import com.marciliojr.pirangueiro.dto.PensamentosDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PensamentosService {

    @Autowired
    private PensamentosRepository pensamentosRepository;

    public String obterMensagemDoDia() {
        return pensamentosRepository.findRandomPensamento()
                .map(Pensamentos::getTexto)
                .orElse("Não há pensamentos cadastrados no momento.");
    }

    public List<PensamentosDTO> listarTodos() {
        return pensamentosRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public PensamentosDTO buscarPorId(Long id) {
        return pensamentosRepository.findById(id)
                .map(this::converterParaDTO)
                .orElseThrow(() -> new RuntimeException("Pensamento não encontrado"));
    }

    public PensamentosDTO salvar(PensamentosDTO pensamentosDTO) {
        Pensamentos pensamentos = converterParaEntidade(pensamentosDTO);
        return converterParaDTO(pensamentosRepository.save(pensamentos));
    }

    public void excluir(Long id) {
        pensamentosRepository.deleteById(id);
    }

    private PensamentosDTO converterParaDTO(Pensamentos pensamentos) {
        PensamentosDTO dto = new PensamentosDTO();
        dto.setId(pensamentos.getId());
        dto.setTexto(pensamentos.getTexto());
        return dto;
    }

    private Pensamentos converterParaEntidade(PensamentosDTO dto) {
        Pensamentos pensamentos = new Pensamentos();
        pensamentos.setId(dto.getId());
        pensamentos.setTexto(dto.getTexto());
        return pensamentos;
    }
} 