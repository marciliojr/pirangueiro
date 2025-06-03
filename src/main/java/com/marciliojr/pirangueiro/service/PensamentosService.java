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

    @Autowired
    private HistoricoService historicoService;

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
        Pensamentos salvos = pensamentosRepository.save(pensamentos);
        
        // Registrar no histórico
        try {
            if (pensamentosDTO.getId() == null) {
                // Criação
                historicoService.registrarCriacaoPensamentos(salvos.getId(), salvos.toString(), null);
            } else {
                // Edição
                historicoService.registrarEdicaoPensamentos(salvos.getId(), salvos.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
        
        return converterParaDTO(salvos);
    }

    public void excluir(Long id) {
        try {
            // Buscar o pensamento antes de excluir para registrar no histórico
            Pensamentos pensamentos = pensamentosRepository.findById(id).orElse(null);
            
            pensamentosRepository.deleteById(id);
            
            // Registrar exclusão no histórico
            if (pensamentos != null) {
                historicoService.registrarExclusaoPensamentos(id, pensamentos.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao excluir pensamento ou registrar histórico: " + e.getMessage());
            throw e;
        }
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