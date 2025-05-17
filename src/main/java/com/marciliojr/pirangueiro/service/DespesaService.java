package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Despesa;
import com.marciliojr.pirangueiro.model.Conta;
import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.model.Categoria;
import com.marciliojr.pirangueiro.repository.DespesaRepository;
import com.marciliojr.pirangueiro.repository.CartaoRepository;
import com.marciliojr.pirangueiro.dto.DespesaDTO;
import com.marciliojr.pirangueiro.dto.ContaDTO;
import com.marciliojr.pirangueiro.dto.CartaoDTO;
import com.marciliojr.pirangueiro.dto.CategoriaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class DespesaService {

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    public List<DespesaDTO> listarTodas() {
        return despesaRepository.findAllWithRelationships().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public DespesaDTO buscarPorId(Long id) {
        return despesaRepository.findByIdWithRelationships(id)
                .map(this::converterParaDTO)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));
    }

    public List<DespesaDTO> buscarPorDescricao(String descricao) {
        return despesaRepository.findByDescricaoContainingWithRelationships(descricao).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<DespesaDTO> buscarPorMesEAno(int mes, int ano) {
        return despesaRepository.findByMesEAnoWithRelationships(mes, ano).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public DespesaDTO salvar(DespesaDTO despesaDTO) {
        Despesa despesa = converterParaEntidade(despesaDTO);
        return converterParaDTO(despesaRepository.save(despesa));
    }

    public void excluir(Long id) {
        despesaRepository.deleteById(id);
    }

    public List<DespesaDTO> buscarPorDescricaoContaCartaoData(String descricao, Long contaId, Long cartaoId, LocalDate data) {
        return despesaRepository.buscarPorDescricaoContaCartaoData(descricao, contaId, cartaoId, data)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<DespesaDTO> buscarDespesasPorCartaoEPeriodoFatura(Long cartaoId, int mes, int ano) {
        // Buscar o cartão para obter o dia de fechamento
        Cartao cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

        // Calcular as datas de início e fim do período da fatura
        LocalDate dataReferencia = LocalDate.of(ano, mes, cartao.getDiaFechamento());
        LocalDate dataFim = dataReferencia;
        LocalDate dataInicio = dataReferencia.minusMonths(1).plusDays(1);

        return despesaRepository.buscarDespesasPorCartaoEPeriodoFatura(cartaoId, dataInicio, dataFim).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public Page<DespesaDTO> buscarPorDescricaoMesAno(String descricao, Integer mes, Integer ano, int pagina) {
        Pageable paginacao = PageRequest.of(pagina, 30);
        return despesaRepository.findByDescricaoAndMesAno(descricao, mes, ano, paginacao)
                .map(this::converterParaDTO);
    }

    public List<DespesaDTO> buscarPorDescricaoMesAnoSemPaginar(String descricao, Integer mes, Integer ano) {
        return despesaRepository.findByDescricaoAndMesAnoSemPaginar(descricao, mes, ano)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public Page<DespesaDTO> buscarComFiltros(String descricao, Integer mes, Integer ano, int pagina, int tamanhoPagina) {
        Pageable paginacao = PageRequest.of(pagina, tamanhoPagina, Sort.by(Sort.Direction.DESC, "data"));
        return despesaRepository.findByFiltros(descricao, mes, ano, paginacao)
                .map(this::converterParaDTO);
    }

    public List<DespesaDTO> buscarComFiltrosSemPaginar(String descricao, Integer mes, Integer ano) {
        List<Despesa> byFiltrosSemPaginar = despesaRepository.findByFiltrosSemPaginar(descricao, mes, ano);

        List<DespesaDTO> collect = byFiltrosSemPaginar.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());

        return collect;
    }

    public Double buscarTotalDespesas() {
        return despesaRepository.buscarTotalDespesas();
    }

    private DespesaDTO converterParaDTO(Despesa despesa) {
        DespesaDTO dto = new DespesaDTO();
        dto.setId(despesa.getId());
        dto.setDescricao(despesa.getDescricao());
        dto.setValor(despesa.getValor());
        dto.setData(despesa.getData());
        
        // Converter e preencher ContaDTO
        if (despesa.getConta() != null) {
            dto.setConta(converterContaParaDTO(despesa.getConta()));
        }
        
        // Converter e preencher CartaoDTO
        if (despesa.getCartao() != null) {
            dto.setCartao(converterCartaoParaDTO(despesa.getCartao()));
        }
        
        // Converter e preencher CategoriaDTO
        if (despesa.getCategoria() != null) {
            dto.setCategoria(converterCategoriaParaDTO(despesa.getCategoria()));
        }
        
        dto.setAnexo(despesa.getAnexo());
        dto.setObservacao(despesa.getObservacao());
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

    private CartaoDTO converterCartaoParaDTO(Cartao cartao) {
        CartaoDTO cartaoDTO = new CartaoDTO();
        cartaoDTO.setId(cartao.getId());
        cartaoDTO.setNome(cartao.getNome());
        cartaoDTO.setLimite(cartao.getLimite());
        cartaoDTO.setDiaFechamento(cartao.getDiaFechamento());
        cartaoDTO.setDiaVencimento(cartao.getDiaVencimento());
        return cartaoDTO;
    }

    private CategoriaDTO converterCategoriaParaDTO(Categoria categoria) {
        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setId(categoria.getId());
        categoriaDTO.setNome(categoria.getNome());
        categoriaDTO.setCor(categoria.getCor());
        categoriaDTO.setImagemCategoria(categoria.getImagemCategoria());
        categoriaDTO.setTipoReceita(categoria.getTipoReceita());
        return categoriaDTO;
    }

    private Despesa converterParaEntidade(DespesaDTO dto) {
        Despesa despesa = new Despesa();
        despesa.setId(dto.getId());
        despesa.setDescricao(dto.getDescricao());
        despesa.setValor(dto.getValor());
        despesa.setData(dto.getData());
        
        // Converter ContaDTO para Conta
        if (dto.getConta() != null) {
            Conta conta = new Conta();
            conta.setId(dto.getConta().getId());
            despesa.setConta(conta);
        }
        
        // Converter CartaoDTO para Cartao
        if (dto.getCartao() != null) {
            Cartao cartao = new Cartao();
            cartao.setId(dto.getCartao().getId());
            despesa.setCartao(cartao);
        }
        
        // Converter CategoriaDTO para Categoria
        if (dto.getCategoria() != null) {
            Categoria categoria = new Categoria();
            categoria.setId(dto.getCategoria().getId());
            despesa.setCategoria(categoria);
        }
        
        despesa.setAnexo(dto.getAnexo());
        despesa.setObservacao(dto.getObservacao());
        return despesa;
    }
} 