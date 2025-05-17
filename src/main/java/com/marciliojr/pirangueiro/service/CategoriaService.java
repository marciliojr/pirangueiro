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
        return converterParaDTO(categoriaRepository.save(categoria));
    }

    public void excluir(Long id) {
        categoriaRepository.deleteById(id);
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
        dto.setImagemCategoria(categoria.getImagemCategoria());
        dto.setTipoReceita(categoria.getTipoReceita());
        return dto;
    }

    private Categoria converterParaEntidade(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setId(dto.getId());
        categoria.setNome(dto.getNome());
        categoria.setCor(dto.getCor());
        categoria.setImagemCategoria(dto.getImagemCategoria());
        categoria.setTipoReceita(dto.getTipoReceita());
        return categoria;
    }
} 