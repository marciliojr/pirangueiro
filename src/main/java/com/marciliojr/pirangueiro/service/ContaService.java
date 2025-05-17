package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Conta;
import com.marciliojr.pirangueiro.repository.ContaRepository;
import com.marciliojr.pirangueiro.dto.ContaDTO;
import com.marciliojr.pirangueiro.dto.SaldoContaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;


    public List<ContaDTO> listarTodas() {
        return contaRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public ContaDTO buscarPorId(Long id) {
        return contaRepository.findById(id)
                .map(this::converterParaDTO)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
    }

    public List<ContaDTO> buscarPorNome(String nome) {
        return contaRepository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public ContaDTO salvar(ContaDTO contaDTO, MultipartFile imagemLogo) {
        try {
            Conta conta = converterParaEntidade(contaDTO);
            return converterParaDTO(contaRepository.save(conta));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar a imagem: " + e.getMessage());
        }
    }

    public void excluir(Long id) {
        contaRepository.deleteById(id);
    }

    public SaldoContaDTO calcularSaldoConta(Long contaId, Integer mes, Integer ano) {
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        Double totalReceitas;
        Double totalDespesas;

        if (mes == null || ano == null) {
            // Se não informar mês e ano, calcula o total geral
            totalReceitas = contaRepository.calcularTotalReceitasPorConta(contaId);
            totalDespesas = contaRepository.calcularTotalDespesasPorConta(contaId);
        } else {
            // Se informar mês e ano, calcula apenas do período específico
            totalReceitas = contaRepository.calcularTotalReceitasPorContaEMes(contaId, mes, ano);
            totalDespesas = contaRepository.calcularTotalDespesasPorContaEMes(contaId, mes, ano);
        }

        Double saldo = totalReceitas - totalDespesas;

        SaldoContaDTO saldoDTO = new SaldoContaDTO();
        saldoDTO.setContaId(contaId);
        saldoDTO.setNomeConta(conta.getNome());
        saldoDTO.setTotalReceitas(totalReceitas);
        saldoDTO.setTotalDespesas(totalDespesas);
        saldoDTO.setSaldo(saldo);
        saldoDTO.setMes(mes);
        saldoDTO.setAno(ano);

        return saldoDTO;
    }

    private ContaDTO converterParaDTO(Conta conta) {
        ContaDTO dto = new ContaDTO();
        dto.setId(conta.getId());
        dto.setNome(conta.getNome());
        dto.setTipo(conta.getTipo());
        dto.setImagemLogo(conta.getImagemLogo());
        return dto;
    }

    private Conta converterParaEntidade(ContaDTO dto) {
        Conta conta = new Conta();
        conta.setId(dto.getId());
        conta.setNome(dto.getNome());
        conta.setTipo(dto.getTipo());
        conta.setImagemLogo(dto.getImagemLogo());
        return conta;
    }
} 