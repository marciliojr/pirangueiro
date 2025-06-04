package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.CartaoDTO;
import com.marciliojr.pirangueiro.dto.CategoriaDTO;
import com.marciliojr.pirangueiro.dto.ContaDTO;
import com.marciliojr.pirangueiro.dto.DespesaDTO;
import com.marciliojr.pirangueiro.exception.NegocioException;
import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.model.Categoria;
import com.marciliojr.pirangueiro.model.Conta;
import com.marciliojr.pirangueiro.model.Despesa;
import com.marciliojr.pirangueiro.repository.CartaoRepository;
import com.marciliojr.pirangueiro.repository.DespesaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DespesaService {

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private HistoricoService historicoService;

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
        Despesa salva = despesaRepository.save(despesa);
        
        // Registrar no histórico
        try {
            if (despesaDTO.getId() == null) {
                // Criação
                historicoService.registrarCriacaoDespesa(salva.getId(), salva.toString(), null);
            } else {
                // Edição
                historicoService.registrarEdicaoDespesa(salva.getId(), salva.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
        
        return converterParaDTO(salva);
    }

    public void salvar(Despesa despesa) {
        despesaRepository.save(despesa);
    }


    public void excluir(Long id) {
        try {
            // Buscar a despesa antes de excluir para registrar no histórico
            Despesa despesa = despesaRepository.findById(id).orElse(null);
            
            despesaRepository.deleteById(id);
            
            // Registrar exclusão no histórico
            if (despesa != null) {
                historicoService.registrarExclusaoDespesa(id, despesa.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao excluir despesa ou registrar histórico: " + e.getMessage());
            throw e;
        }
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

    public List<Despesa> buscarDespesasPorCartao(
            Long cartaoId) {
        return despesaRepository.buscarDespesasPorCartao(cartaoId);
    }

    public Double buscarTotalDespesas() {
        return despesaRepository.buscarTotalDespesas();
    }

    public Double buscarTotalDespesasNaoPagas() {
        return despesaRepository.buscarTotalDespesasNaoPagas();
    }

    public void marcarDespesaComoPaga(Long despesaId) {
        Despesa despesa = despesaRepository.findById(despesaId)
                .orElseThrow(() -> new NegocioException("Despesa não encontrada com ID: " + despesaId));
        despesa.setPago(true);
        despesaRepository.save(despesa);
    }

    private void validarLimiteCartaoDeCredito(DespesaDTO despesaDTO) {
        if (despesaDTO.getCartao() != null) {
            Long cartaoId = despesaDTO.getCartao().getId();

            Cartao cartao = cartaoRepository.findById(cartaoId)
                    .orElseThrow(() -> new NegocioException("Cartão não encontrado"));

            Double totalCompras = cartaoRepository.calcularTotalDespesasPorCartao(cartaoId);

            if (despesaDTO.getId() != null) {
                Despesa despesaExistente = despesaRepository.findById(despesaDTO.getId())
                        .orElseThrow(() -> new NegocioException("Despesa não encontrada para edição"));

                Long cartaoIdDespesaExistente = despesaExistente.getCartao() != null
                        ? despesaExistente.getCartao().getId()
                        : null;

                // Subtrai valor antigo apenas se for o mesmo cartão e despesa ainda estiver pendente
                if (cartaoId.equals(cartaoIdDespesaExistente) && !despesaExistente.getPago()) {
                    totalCompras -= despesaExistente.getValor();
                }
            }

            if (totalCompras + despesaDTO.getValor() > cartao.getLimite()) {
                throw new NegocioException(
                        "Limite do cartão excedido",
                        "422",
                        String.format(
                                "O Limite atual do cartão é: %.2f, o total atual de compras é: %.2f, e o total disponível para compras é: %.2f",
                                cartao.getLimite(),
                                totalCompras,
                                cartao.getLimite() - totalCompras
                        )
                );
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
            parcela.setExtensaoAnexo(despesaDTO.getExtensaoAnexo());
            parcela.setObservacao(despesaDTO.getObservacao());
            parcela.setExtensaoAnexo(despesaDTO.getExtensaoAnexo());
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
        dto.setExtensaoAnexo(despesa.getExtensaoAnexo());
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
        despesa.setExtensaoAnexo(dto.getExtensaoAnexo());
        despesa.setAnexo(dto.getAnexo());
        despesa.setObservacao(dto.getObservacao());
        return despesa;
    }
} 