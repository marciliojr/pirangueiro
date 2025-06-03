package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Categoria;
import com.marciliojr.pirangueiro.repository.CategoriaRepository;
import com.marciliojr.pirangueiro.dto.CategoriaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private HistoricoService historicoService;

    public List<CategoriaDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .sorted(Comparator.comparing(Categoria::getTipoReceita).reversed())
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(this::converterParaDTO)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
    }

    public List<CategoriaDTO> buscarPorNome(String nome) {
        return categoriaRepository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO salvar(CategoriaDTO categoriaDTO) {
        Categoria categoria = converterParaEntidade(categoriaDTO);
        Categoria salva = categoriaRepository.save(categoria);
        
        // Registrar no histórico
        try {
            if (categoriaDTO.getId() == null) {
                // Criação
                historicoService.registrarCriacaoCategoria(salva.getId(), salva.toString(), null);
            } else {
                // Edição
                historicoService.registrarEdicaoCategoria(salva.getId(), salva.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
        
        return converterParaDTO(salva);
    }

    public void excluir(Long id) {
        try {
            // Buscar a categoria antes de excluir para registrar no histórico
            Categoria categoria = categoriaRepository.findById(id).orElse(null);
            
            categoriaRepository.deleteById(id);
            
            // Registrar exclusão no histórico
            if (categoria != null) {
                historicoService.registrarExclusaoCategoria(id, categoria.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao excluir categoria ou registrar histórico: " + e.getMessage());
            throw e;
        }
    }

    public List<CategoriaDTO> buscarPorTipoReceita(Boolean tipoReceita) {
        return categoriaRepository.findByTipoReceita(tipoReceita).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    private CategoriaDTO converterParaDTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getId());
        dto.setNome(categoria.getNome());
        dto.setCor(categoria.getCor());
        dto.setTipoReceita(categoria.getTipoReceita());
        return dto;
    }

    private Categoria converterParaEntidade(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setId(dto.getId());
        categoria.setNome(dto.getNome());
        categoria.setCor(dto.getCor());
        categoria.setTipoReceita(dto.getTipoReceita());
        return categoria;
    }
} 