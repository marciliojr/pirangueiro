package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.dto.ContaDTO;
import com.marciliojr.pirangueiro.dto.SaldoContaDTO;
import com.marciliojr.pirangueiro.exception.NegocioException;
import com.marciliojr.pirangueiro.model.Conta;
import com.marciliojr.pirangueiro.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private HistoricoService historicoService;

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
            // Processar a imagem se fornecida
            if (imagemLogo != null && !imagemLogo.isEmpty()) {
                contaDTO.setImagemLogo(imagemLogo.getBytes());
            }
            
            Conta conta = converterParaEntidade(contaDTO);
            Conta salva = contaRepository.save(conta);
            
            // Registrar no histórico
            try {
                if (contaDTO.getId() == null) {
                    // Criação
                    historicoService.registrarCriacaoConta(salva.getId(), salva.toString(), null);
                } else {
                    // Edição
                    historicoService.registrarEdicaoConta(salva.getId(), salva.toString(), null);
                }
            } catch (Exception e) {
                // Log do erro mas não falha a operação principal
                System.err.println("Erro ao registrar histórico: " + e.getMessage());
            }
            
            return converterParaDTO(salva);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar a imagem: " + e.getMessage());
        }
    }

    public void excluir(Long id) {

        if (existeReceitaAssociadaConta(id)) {
            throw new NegocioException("Não é possível excluir a conta, pois existem receitas associadas a ela.");
        }

        if (existeDespesaAssociadaConta(id)) {
            throw new NegocioException("Não é possível excluir a conta, pois existem despesas associadas a ela.");
        }

        try {
            // Buscar a conta antes de excluir para registrar no histórico
            Conta conta = contaRepository.findById(id).orElse(null);
            
            contaRepository.deleteById(id);
            
            // Registrar exclusão no histórico
            if (conta != null) {
                historicoService.registrarExclusaoConta(id, conta.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao excluir conta ou registrar histórico: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Calcula o saldo de uma conta considerando receitas, despesas diretas da conta
     * e despesas de cartão não pagas.
     * 
     * @param contaId ID da conta para cálculo
     * @param mes Mês específico (opcional)
     * @param ano Ano específico (opcional)
     * @return DTO com informações detalhadas do saldo
     */
    public SaldoContaDTO calcularSaldoConta(Long contaId, Integer mes, Integer ano) {
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        Double totalReceitas;
        Double totalDespesas;
        Double totalDespesasCartao;

        if (mes == null || ano == null) {
            // Se não informar mês e ano, calcula o total geral
            totalReceitas = contaRepository.calcularTotalReceitasPorConta(contaId);
            totalDespesas = contaRepository.calcularTotalDespesasPorConta(contaId);
            totalDespesasCartao = contaRepository.calcularTotalDespesasCartaoNaoPagas();
        } else {
            // Se informar mês e ano, calcula apenas do período específico
            totalReceitas = contaRepository.calcularTotalReceitasPorContaEMes(contaId, mes, ano);
            totalDespesas = contaRepository.calcularTotalDespesasPorContaEMes(contaId, mes, ano);
            totalDespesasCartao = contaRepository.calcularTotalDespesasCartaoNaoPagasPorMesAno(mes, ano);
        }

        // Garantir que os valores não sejam null
        totalReceitas = totalReceitas != null ? totalReceitas : 0.0;
        totalDespesas = totalDespesas != null ? totalDespesas : 0.0;
        totalDespesasCartao = totalDespesasCartao != null ? totalDespesasCartao : 0.0;

        // Somar despesas diretas da conta + despesas de cartão não pagas
        Double totalDespesasCompleto = totalDespesas + totalDespesasCartao;
        Double saldo = totalReceitas - totalDespesasCompleto;

        SaldoContaDTO saldoDTO = new SaldoContaDTO();
        saldoDTO.setContaId(contaId);
        saldoDTO.setNomeConta(conta.getNome());
        saldoDTO.setTotalReceitas(totalReceitas);
        saldoDTO.setTotalDespesas(totalDespesasCompleto);
        saldoDTO.setTotalDespesasConta(totalDespesas);
        saldoDTO.setTotalDespesasCartao(totalDespesasCartao);
        saldoDTO.setSaldo(saldo);
        saldoDTO.setMes(mes);
        saldoDTO.setAno(ano);

        return saldoDTO;
    }

    public boolean existeDespesaAssociadaConta(Long contaId) {
        return contaRepository.existeDespesaAssociadaConta(contaId);
    }

    public boolean existeReceitaAssociadaConta(Long contaId) {
        return contaRepository.existeReceitaAssociadaConta(contaId);
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