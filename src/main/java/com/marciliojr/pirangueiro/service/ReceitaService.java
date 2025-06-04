package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Receita;
import com.marciliojr.pirangueiro.model.Conta;
import com.marciliojr.pirangueiro.model.Categoria;
import com.marciliojr.pirangueiro.repository.ReceitaRepository;
import com.marciliojr.pirangueiro.dto.ReceitaDTO;
import com.marciliojr.pirangueiro.dto.ContaDTO;
import com.marciliojr.pirangueiro.dto.CategoriaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceitaService {

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private ContaService contaService;

    @Autowired
    private HistoricoService historicoService;

    public List<ReceitaDTO> listarTodas() {
        return receitaRepository.findAllWithRelationships().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public ReceitaDTO buscarPorId(Long id) {
        return receitaRepository.findByIdWithRelationships(id)
                .map(this::converterParaDTO)
                .orElseThrow(() -> new RuntimeException("Receita não encontrada"));
    }

    public List<ReceitaDTO> buscarPorDescricao(String descricao) {
        return receitaRepository.findByDescricaoContainingWithRelationships(descricao).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<ReceitaDTO> buscarPorMesEAno(int mes, int ano) {
        return receitaRepository.findByMesEAnoWithRelationships(mes, ano).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public ReceitaDTO salvar(ReceitaDTO receitaDTO) {
        Receita receita = converterParaEntidade(receitaDTO);
        Receita salva = receitaRepository.save(receita);
        
        // Registrar no histórico
        try {
            if (receitaDTO.getId() == null) {
                // Criação
                historicoService.registrarCriacaoReceita(salva.getId(), salva.toString(), null);
            } else {
                // Edição
                historicoService.registrarEdicaoReceita(salva.getId(), salva.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
        
        return converterParaDTO(salva);
    }

    public void excluir(Long id) {
        try {
            // Buscar a receita antes de excluir para registrar no histórico
            Receita receita = receitaRepository.findById(id).orElse(null);
            
            receitaRepository.deleteById(id);
            
            // Registrar exclusão no histórico
            if (receita != null) {
                historicoService.registrarExclusaoReceita(id, receita.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao excluir receita ou registrar histórico: " + e.getMessage());
            throw e;
        }
    }

    public Double buscarTotalReceitas() {
        return receitaRepository.buscarTotalReceitas();
    }

    public Page<ReceitaDTO> buscarPorMesEAnoPaginado(int mes, int ano, int pagina, int tamanhoPagina) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, Sort.by(Sort.Direction.DESC, "data"));
        return receitaRepository.findByMesEAnoWithRelationshipsPaged(mes, ano, pageable)
                .map(this::converterParaDTO);
    }

    public Page<ReceitaDTO> buscarComFiltros(
            String descricao,
            Integer mes,
            Integer ano,
            int pagina,
            int tamanhoPagina,
            String ordenacao,
            String direcao) {
        
        Sort sort;
        if (ordenacao == null || ordenacao.isEmpty()) {
            sort = Sort.by(Sort.Direction.DESC, "data"); // ordenação padrão
        } else {
            Sort.Direction direction = direcao != null && direcao.equalsIgnoreCase("ASC") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, ordenacao);
        }

        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, sort);
        return receitaRepository.findByFiltros(descricao, mes, ano, pageable)
                .map(this::converterParaDTO);
    }

    private ReceitaDTO converterParaDTO(Receita receita) {
        ReceitaDTO dto = new ReceitaDTO();
        dto.setId(receita.getId());
        dto.setDescricao(receita.getDescricao());
        dto.setValor(receita.getValor());
        dto.setData(receita.getData());

        // Converter e preencher ContaDTO 
        if (receita.getConta() != null) {
            dto.setConta(converterContaParaDTO(receita.getConta()));
        }

        // Converter e preencher CategoriaDTO
        if (receita.getCategoria() != null) {
            dto.setCategoria(converterCategoriaParaDTO(receita.getCategoria()));
        }

        dto.setAnexo(receita.getAnexo());
        dto.setExtensaoAnexo(receita.getExtensaoAnexo());
        dto.setObservacao(receita.getObservacao());
        return dto;
    }

    private ContaDTO converterContaParaDTO(Conta conta) {
        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setId(conta.getId());
        contaDTO.setNome(conta.getNome());
        contaDTO.setTipo(conta.getTipo());
        contaDTO.setImagemLogo(conta.getImagemLogo());
        return contaDTO;
    }

    private CategoriaDTO converterCategoriaParaDTO(Categoria categoria) {
        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setId(categoria.getId());
        categoriaDTO.setNome(categoria.getNome());
        categoriaDTO.setCor(categoria.getCor());
        categoriaDTO.setTipoReceita(categoria.getTipoReceita());
        return categoriaDTO;
    }

    private Receita converterParaEntidade(ReceitaDTO dto) {
        Receita receita = new Receita();
        receita.setId(dto.getId());
        receita.setDescricao(dto.getDescricao());
        receita.setValor(dto.getValor());
        receita.setData(dto.getData());

        // Converter ContaDTO para Conta
        if (dto.getConta() != null) {
            Conta conta = new Conta();
            conta.setId(dto.getConta().getId());
            receita.setConta(conta);
        }

        // Converter CategoriaDTO para Categoria
        if (dto.getCategoria() != null) {
            Categoria categoria = new Categoria();
            categoria.setId(dto.getCategoria().getId());
            receita.setCategoria(categoria);
        }
        receita.setExtensaoAnexo(dto.getExtensaoAnexo());
        receita.setAnexo(dto.getAnexo());
        receita.setObservacao(dto.getObservacao());
        return receita;
    }
} 