package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.exception.NegocioException;
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

        validarLimiteCartaoDeCredito(despesaDTO);

        if (despesaDTO.getQuantidadeParcelas() != null && despesaDTO.getQuantidadeParcelas() > 1) {
            return salvarDespesaParcelada(despesaDTO);
        }
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

        return byFiltrosSemPaginar.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public Double buscarTotalDespesas() {
        return despesaRepository.buscarTotalDespesas();
    }

    public void marcarDespesaComoPaga(Long despesaId) {
        Despesa despesa = despesaRepository.findById(despesaId)
                .orElseThrow(() -> new NegocioException("Despesa não encontrada com ID: " + despesaId));
        despesa.setPago(true);
        despesaRepository.save(despesa);
    }

    private void validarLimiteCartaoDeCredito(DespesaDTO despesaDTO) {
        if (despesaDTO.getCartao() != null) {
            Cartao cartao = cartaoRepository.findById(despesaDTO.getCartao().getId())
                    .orElseThrow(() -> new NegocioException("Cartão não encontrado"));
            Double totalCompras = cartaoRepository.calcularTotalDespesasPorCartao(despesaDTO.getCartao().getId());
            if (totalCompras + despesaDTO.getValor() > cartao.getLimite()) {
                throw new NegocioException("Limite do cartão excedido", "422", "O Limite atual do cartão é: "
                        + cartao.getLimite()
                        + " e o total atual de compras é: "
                        + totalCompras + " e o total disponível para compras é: " + (cartao.getLimite() - totalCompras));
            }
        }
    }

    private DespesaDTO salvarDespesaParcelada(DespesaDTO despesaDTO) {
        int quantidadeParcelas = despesaDTO.getQuantidadeParcelas();
        double valorParcela = despesaDTO.getValor() / quantidadeParcelas;
        LocalDate dataBase = despesaDTO.getData();
        DespesaDTO primeiraParcela = null;

        for (int i = 1; i <= quantidadeParcelas; i++) {
            DespesaDTO parcela = new DespesaDTO();
            parcela.setDescricao(despesaDTO.getDescricao() + " (" + i + "/" + quantidadeParcelas + ")");
            parcela.setValor(valorParcela);
            parcela.setData(dataBase.plusMonths(i - 1));
            parcela.setConta(despesaDTO.getConta());
            parcela.setCartao(despesaDTO.getCartao());
            parcela.setCategoria(despesaDTO.getCategoria());
            parcela.setAnexo(despesaDTO.getAnexo());
            parcela.setObservacao(despesaDTO.getObservacao());
            parcela.setPago(despesaDTO.getPago());
            parcela.setNumeroParcela(i);
            parcela.setTotalParcelas(quantidadeParcelas);

            Despesa despesa = converterParaEntidade(parcela);
            Despesa salva = despesaRepository.save(despesa);

            if (i == 1) {
                primeiraParcela = converterParaDTO(salva);
            }
        }

        return primeiraParcela;
    }

    private DespesaDTO converterParaDTO(Despesa despesa) {
        DespesaDTO dto = new DespesaDTO();
        dto.setId(despesa.getId());
        dto.setDescricao(despesa.getDescricao());
        dto.setValor(despesa.getValor());
        dto.setData(despesa.getData());
        dto.setNumeroParcela(despesa.getNumeroParcela());
        dto.setTotalParcelas(despesa.getTotalParcelas());
        dto.setPago(despesa.getPago());

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
        despesa.setNumeroParcela(dto.getNumeroParcela());
        despesa.setTotalParcelas(dto.getTotalParcelas());
        despesa.setPago(dto.getPago());

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