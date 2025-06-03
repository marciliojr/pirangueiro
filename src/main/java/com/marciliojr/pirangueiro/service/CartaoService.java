package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.CartaoDTO;
import com.marciliojr.pirangueiro.exception.NegocioException;
import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.model.Despesa;
import com.marciliojr.pirangueiro.repository.CartaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CartaoService {

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private DespesaService despesaService;

    @Autowired
    private HistoricoService historicoService;

    public List<CartaoDTO> listarTodos() {
        return cartaoRepository.findAll().stream().map(this::converterParaDTO).collect(Collectors.toList());
    }

    public CartaoDTO buscarPorId(Long id) {
        return cartaoRepository.findById(id).map(this::converterParaDTO).orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
    }

    public List<CartaoDTO> buscarPorNome(String nome) {
        return cartaoRepository.findByNomeContainingIgnoreCase(nome).stream().map(this::converterParaDTO).collect(Collectors.toList());
    }

    public CartaoDTO salvar(CartaoDTO cartaoDTO) {
        Cartao cartao = converterParaEntidade(cartaoDTO);
        Cartao salvo = cartaoRepository.save(cartao);
        
        // Registrar no histórico
        try {
            if (cartaoDTO.getId() == null) {
                // Criação
                historicoService.registrarCriacaoCartao(salvo.getId(), salvo.toString(), null);
            } else {
                // Edição
                historicoService.registrarEdicaoCartao(salvo.getId(), salvo.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
        
        return converterParaDTO(salvo);
    }

    public CartaoDTO atualizar(CartaoDTO cartaoDTO) {
        Cartao cartao = converterParaEntidade(cartaoDTO);
        Double limiteUsado = calcularLimiteUsado(cartao.getId());

        if (cartao.getLimite() == null || cartao.getLimite() <= 0) {
            throw new NegocioException("Erro ao Atualizar", "422", "Limite do cartão deve ser maior que zero.");
        }

        if (cartao.getLimite() < limiteUsado) {
            throw new NegocioException("Erro ao Atualizar", "422", "Limite do cartão não pode ser menor que o limite usado.");
        }

        Cartao salvo = cartaoRepository.save(cartao);
        
        // Registrar edição no histórico
        try {
            historicoService.registrarEdicaoCartao(salvo.getId(), salvo.toString(), null);
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }

        return converterParaDTO(salvo);
    }

    public void excluir(Long id, boolean manterDespesas) {
        try {
            // Buscar o cartão antes de excluir para registrar no histórico
            Cartao cartao = cartaoRepository.findById(id).orElse(null);
            
            if (manterDespesas) {
                List<Despesa> despesas = despesaService.buscarDespesasPorCartao(id);
                String nomeCartao = despesas.stream()
                        .map(Despesa::getCartao)
                        .filter(Objects::nonNull)
                        .map(Cartao::getNome)
                        .findFirst()
                        .orElse("");

                double valorTotalDespesas = despesas.stream()
                        .mapToDouble(Despesa::getValor)
                        .sum();

                LocalDate dataCompra = despesas.stream()
                        .map(Despesa::getData)
                        .findFirst()
                        .orElse(null);

                despesas.forEach(despesa -> despesaService.excluir(despesa.getId()));

                Despesa despesaHistorica = new Despesa();
                despesaHistorica.setDescricao("Registro historico das despesas do cartão " + nomeCartao);
                despesaHistorica.setValor(valorTotalDespesas);
                despesaHistorica.setObservacao("Despesa para mostrar que o cartão foi excluído e manter o historico");
                despesaHistorica.setData(dataCompra);
                despesaService.salvar(despesaHistorica);
            } else {
                validarExclusao(id);
            }
            
            cartaoRepository.deleteById(id);
            
            // Registrar exclusão no histórico
            if (cartao != null) {
                historicoService.registrarExclusaoCartao(id, cartao.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao excluir cartão ou registrar histórico: " + e.getMessage());
            throw e;
        }
    }

    public void validarExclusao(Long id) {
        boolean existe = cartaoRepository.existeDespesasPorCartao(id);
        if (existe) {
            throw new NegocioException("Erro ao Excluir", "422", "Cartão não pode ser excluído, pois possui despesas associadas.");
        }
    }

    public Double calcularLimiteDisponivel(Long id) {
        Cartao cartao = cartaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
        Double totalDespesas = cartaoRepository.calcularTotalDespesasPorCartao(id);
        return cartao.getLimite() - totalDespesas;
    }

    public Double calcularLimiteUsado(Long id) {
        Cartao cartao = cartaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
        return cartaoRepository.calcularTotalDespesasPorCartao(id);
    }

    private CartaoDTO converterParaDTO(Cartao cartao) {
        CartaoDTO dto = new CartaoDTO();
        dto.setId(cartao.getId());
        dto.setNome(cartao.getNome());
        dto.setLimite(cartao.getLimite());
        dto.setLimiteUsado(cartao.getLimiteUsado());
        dto.setDiaFechamento(cartao.getDiaFechamento());
        dto.setDiaVencimento(cartao.getDiaVencimento());
        return dto;
    }

    private Cartao converterParaEntidade(CartaoDTO dto) {
        Cartao cartao = new Cartao();
        cartao.setId(dto.getId());
        cartao.setNome(dto.getNome());
        cartao.setLimite(dto.getLimite());
        cartao.setLimiteUsado(dto.getLimiteUsado());
        cartao.setDiaFechamento(dto.getDiaFechamento());
        cartao.setDiaVencimento(dto.getDiaVencimento());
        return cartao;
    }
} 