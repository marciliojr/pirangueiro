package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.repository.CartaoRepository;
import com.marciliojr.pirangueiro.dto.CartaoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartaoService {

    @Autowired
    private CartaoRepository cartaoRepository;

    public List<CartaoDTO> listarTodos() {
        return cartaoRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public CartaoDTO buscarPorId(Long id) {
        return cartaoRepository.findById(id)
                .map(this::converterParaDTO)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
    }

    public List<CartaoDTO> buscarPorNome(String nome) {
        return cartaoRepository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public CartaoDTO salvar(CartaoDTO cartaoDTO) {
        Cartao cartao = converterParaEntidade(cartaoDTO);
        return converterParaDTO(cartaoRepository.save(cartao));
    }

    public void excluir(Long id) {
        cartaoRepository.deleteById(id);
    }

    public Double calcularLimiteDisponivel(Long id) {
        Cartao cartao = cartaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
        Double totalDespesas = cartaoRepository.calcularTotalDespesasPorCartao(id);
        return cartao.getLimite() - totalDespesas;
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