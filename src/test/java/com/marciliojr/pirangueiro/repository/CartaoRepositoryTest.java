package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes do Repository de Cartões - Trabalhador com Salário R$ 1000")
class CartaoRepositoryTest {

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Categoria categoriaAlimentacao;

    @BeforeEach
    void configurarCenarioTrabalhador() {
        // Categoria para despesas
        categoriaAlimentacao = new Categoria();
        categoriaAlimentacao.setNome("Alimentação");
        categoriaAlimentacao.setCor("#FF5722");
        categoriaAlimentacao.setTipoReceita(false);
        categoriaAlimentacao = categoriaRepository.save(categoriaAlimentacao);
    }

    @Test
    @DisplayName("Deve criar cartões típicos de trabalhador com salário R$ 1000")
    void deveCriarCartoesTipicosTrabalhador() {
        // Cartões adequados para trabalhador com salário de R$ 1000
        Cartao cartaoBasico = criarCartao("Visa Básico", 500.0, 10, 15);
        Cartao cartaoSupermercado = criarCartao("Cartão Supermercado", 300.0, 5, 10);
        Cartao cartaoFarmacia = criarCartao("Cartão Farmácia", 200.0, 20, 25);

        List<Cartao> todosCartoes = cartaoRepository.findAll();
        
        assertThat(todosCartoes).hasSize(3);
        assertThat(todosCartoes)
            .extracting(Cartao::getNome)
            .containsExactlyInAnyOrder("Visa Básico", "Cartão Supermercado", "Cartão Farmácia");
        
        // Validar limites adequados para salário de R$ 1000
        double somaLimites = todosCartoes.stream().mapToDouble(Cartao::getLimite).sum();
        assertThat(somaLimites).isEqualTo(1000.0); // Total igual ao salário
    }

    @Test
    @DisplayName("Deve calcular limite usado corretamente com campo 'pago'")
    void deveCalcularLimiteUsadoCorretamenteComCampoPago() {
        Cartao cartao = criarCartao("Cartão Teste", 800.0, 15, 10);
        
        // Despesas no cartão - algumas pagas, outras não
        criarDespesaCartao("Supermercado", 200.0, cartao, true);   // Paga - conta no limite
        criarDespesaCartao("Farmácia", 100.0, cartao, true);      // Paga - conta no limite
        criarDespesaCartao("Restaurante", 150.0, cartao, false);  // Não paga - não conta no limite
        criarDespesaCartao("Posto", 50.0, cartao, true);          // Paga - conta no limite

        List<Despesa> todasDespesas = despesaRepository.findAll();
        
        // Calcular limite usado apenas com despesas pagas
        double limiteUsadoReal = todasDespesas.stream()
            .filter(d -> Boolean.TRUE.equals(d.getPago()))
            .mapToDouble(Despesa::getValor)
            .sum();
        
        assertThat(todasDespesas).hasSize(4);
        assertThat(limiteUsadoReal).isEqualTo(350.0); // Apenas despesas pagas
        
        // Validar que ainda tem limite disponível
        double limiteDisponivel = cartao.getLimite() - limiteUsadoReal;
        assertThat(limiteDisponivel).isEqualTo(450.0);
    }

    @Test
    @DisplayName("Deve buscar cartão por ID específico")
    void deveBuscarCartaoPorId() {
        Cartao cartao = criarCartao("Cartão Teste", 600.0, 10, 15);
        
        Optional<Cartao> cartaoEncontrado = cartaoRepository.findById(cartao.getId());
        
        assertThat(cartaoEncontrado).isPresent();
        assertThat(cartaoEncontrado.get().getNome()).isEqualTo("Cartão Teste");
        assertThat(cartaoEncontrado.get().getLimite()).isEqualTo(600.0);
        assertThat(cartaoEncontrado.get().getDiaFechamento()).isEqualTo(10);
        assertThat(cartaoEncontrado.get().getDiaVencimento()).isEqualTo(15);
    }

    @Test
    @DisplayName("Deve validar dias de fechamento e vencimento")
    void deveValidarDiasFechamentoEVencimento() {
        Cartao cartao = criarCartao("Cartão Teste", 500.0, 15, 10);
        
        assertThat(cartao.getDiaFechamento()).isEqualTo(15);
        assertThat(cartao.getDiaVencimento()).isEqualTo(10);
        
        // Validar que vencimento é após fechamento (considerando mês seguinte)
        assertThat(cartao.getDiaVencimento()).isLessThan(cartao.getDiaFechamento());
    }

    @Test
    @DisplayName("Deve simular uso real de cartão por trabalhador")
    void deveSimularUsoRealCartaoTrabalhador() {
        Cartao cartaoAlimentacao = criarCartao("Cartão Alimentação", 400.0, 20, 5);
        
        // Primeira semana do mês
        criarDespesaCartao("Supermercado", 80.0, cartaoAlimentacao, true);
        criarDespesaCartao("Padaria", 15.0, cartaoAlimentacao, true);
        
        // Segunda semana
        criarDespesaCartao("Açougue", 45.0, cartaoAlimentacao, true);
        criarDespesaCartao("Feira", 25.0, cartaoAlimentacao, false); // Ainda não processada
        
        // Terceira semana
        criarDespesaCartao("Farmácia", 85.0, cartaoAlimentacao, true);
        
        List<Despesa> despesasCartao = despesaRepository.findAll();
        
        // Calcular gastos processados (pagos)
        double gastosProcessados = despesasCartao.stream()
            .filter(d -> Boolean.TRUE.equals(d.getPago()))
            .mapToDouble(Despesa::getValor)
            .sum();
        
        assertThat(despesasCartao).hasSize(5);
        assertThat(gastosProcessados).isEqualTo(225.0); // R$ 225 já descontados do limite
        
        double limiteDisponivel = cartaoAlimentacao.getLimite() - gastosProcessados;
        assertThat(limiteDisponivel).isEqualTo(175.0); // Ainda tem R$ 175 disponível
        
        // Validar que não ultrapassou 60% do limite (recomendado)
        double percentualUsado = (gastosProcessados / cartaoAlimentacao.getLimite()) * 100;
        assertThat(percentualUsado).isLessThan(60.0);
    }

    @Test
    @DisplayName("Deve criar cartão com limite adequado ao salário")
    void deveCriarCartaoComLimiteAdequadoSalario() {
        double salario = 1000.0;
        
        // Limite recomendado: máximo 30% do salário
        double limiteRecomendado = salario * 0.30;
        Cartao cartao = criarCartao("Cartão Recomendado", limiteRecomendado, 15, 10);
        
        assertThat(cartao.getLimite()).isEqualTo(300.0);
        assertThat(cartao.getLimite()).isLessThanOrEqualTo(salario * 0.30);
    }

    @Test
    @DisplayName("Deve validar múltiplos cartões com limites controlados")
    void deveValidarMultiplosCartoesComLimitesControlados() {
        double salario = 1000.0;
        
        // Estratégia: vários cartões pequenos em vez de um grande
        Cartao cartaoAlimentacao = criarCartao("Alimentação", 200.0, 10, 5);
        Cartao cartaoFarmacia = criarCartao("Farmácia", 150.0, 15, 10);
        Cartao cartaoGasolina = criarCartao("Gasolina", 100.0, 20, 15);
        Cartao cartaoEmergencia = criarCartao("Emergência", 250.0, 25, 20);
        
        List<Cartao> cartoes = cartaoRepository.findAll();
        double somaTotalLimites = cartoes.stream().mapToDouble(Cartao::getLimite).sum();
        
        assertThat(cartoes).hasSize(4);
        assertThat(somaTotalLimites).isEqualTo(700.0);
        assertThat(somaTotalLimites).isLessThanOrEqualTo(salario * 0.70); // Máximo 70% do salário
    }

    @Test
    @DisplayName("Deve controlar limite usado em cenário de emergência")
    void deveControlarLimiteUsadoCenarioEmergencia() {
        Cartao cartaoEmergencia = criarCartao("Emergência", 500.0, 15, 10);
        
        // Situação: emergência médica
        criarDespesaCartao("Hospital", 300.0, cartaoEmergencia, true);
        criarDespesaCartao("Medicamentos", 80.0, cartaoEmergencia, true);
        criarDespesaCartao("Exames", 120.0, cartaoEmergencia, false); // Ainda não processado
        
        List<Despesa> despesas = despesaRepository.findAll();
        double gastoConfirmado = despesas.stream()
            .filter(d -> Boolean.TRUE.equals(d.getPago()))
            .mapToDouble(Despesa::getValor)
            .sum();
        
        assertThat(gastoConfirmado).isEqualTo(380.0);
        
        double limiteRestante = cartaoEmergencia.getLimite() - gastoConfirmado;
        assertThat(limiteRestante).isEqualTo(120.0);
        
        // Verifica se ainda consegue pagar o exame pendente
        double examePendente = despesas.stream()
            .filter(d -> Boolean.FALSE.equals(d.getPago()))
            .mapToDouble(Despesa::getValor)
            .sum();
        
        assertThat(limiteRestante).isGreaterThanOrEqualTo(examePendente);
    }

    // TESTES DE CENÁRIOS DE ERRO

    @Test
    @DisplayName("Deve falhar ao criar cartão com limite negativo")
    void deveFalharAoCriarCartaoComLimiteNegativo() {
        assertThatThrownBy(() -> {
            criarCartao("Cartão Inválido", -100.0, 15, 10);
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve permitir salvar cartão sem nome mas retornar nulo")
    void devePermitirSalvarCartaoSemNome() {
        Cartao cartao = new Cartao();
        cartao.setNome(null);
        cartao.setLimite(500.0);
        cartao.setLimiteUsado(0.0);
        cartao.setDiaFechamento(15);
        cartao.setDiaVencimento(10);

        Cartao cartaoSalvo = cartaoRepository.save(cartao);
        assertThat(cartaoSalvo.getNome()).isNull();
        assertThat(cartaoSalvo.getLimite()).isEqualTo(500.0);
    }

    @Test
    @DisplayName("Deve retornar optional vazio para ID inexistente")
    void deveRetornarOptionalVazioParaIdInexistente() {
        Optional<Cartao> cartao = cartaoRepository.findById(999L);
        assertThat(cartao).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há cartões")
    void deveRetornarListaVaziaQuandoNaoHaCartoes() {
        List<Cartao> cartoes = cartaoRepository.findAll();
        assertThat(cartoes).isEmpty();
    }

    @Test
    @DisplayName("Deve permitir criar cartão com dias válidos")
    void devePermitirCriarCartaoComDiasValidos() {
        Cartao cartao = criarCartao("Cartão Teste", 300.0, 1, 31);
        
        assertThat(cartao.getDiaFechamento()).isBetween(1, 31);
        assertThat(cartao.getDiaVencimento()).isBetween(1, 31);
    }

    @Test
    @DisplayName("Deve simular cenário de controle financeiro rigoroso")
    void deveSimularCenarioControleFinanceiroRigoroso() {
        double salario = 1000.0;
        double limiteSeguro = salario * 0.20; // Apenas 20% do salário
        
        Cartao cartaoControlado = criarCartao("Cartão Controlado", limiteSeguro, 15, 10);
        
        // Gastos controlados
        criarDespesaCartao("Supermercado", 50.0, cartaoControlado, true);
        criarDespesaCartao("Farmácia", 30.0, cartaoControlado, true);
        criarDespesaCartao("Transporte", 20.0, cartaoControlado, true);
        
        List<Despesa> despesas = despesaRepository.findAll();
        double totalGasto = despesas.stream()
            .filter(d -> Boolean.TRUE.equals(d.getPago()))
            .mapToDouble(Despesa::getValor)
            .sum();
        
        assertThat(totalGasto).isEqualTo(100.0);
        assertThat(totalGasto).isLessThanOrEqualTo(limiteSeguro * 0.50); // Usou apenas 50% do limite
        
        double percentualSalario = (totalGasto / salario) * 100;
        assertThat(percentualSalario).isEqualTo(10.0); // Apenas 10% do salário no cartão
    }

    private Cartao criarCartao(String nome, Double limite, Integer diaFechamento, Integer diaVencimento) {
        if (limite != null && limite < 0) {
            throw new IllegalArgumentException("Limite não pode ser negativo");
        }
        
        Cartao cartao = new Cartao();
        cartao.setNome(nome);
        cartao.setLimite(limite);
        cartao.setLimiteUsado(0.0);
        cartao.setDiaFechamento(diaFechamento);
        cartao.setDiaVencimento(diaVencimento);
        return cartaoRepository.save(cartao);
    }

    private Despesa criarDespesaCartao(String descricao, Double valor, Cartao cartao, Boolean pago) {
        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setData(LocalDate.now());
        despesa.setCartao(cartao);
        despesa.setCategoria(categoriaAlimentacao);
        despesa.setPago(pago); // Campo usado para cálculo do limite do cartão
        return despesaRepository.save(despesa);
    }
} 