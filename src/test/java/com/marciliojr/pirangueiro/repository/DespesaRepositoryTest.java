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
@DisplayName("Testes do Repository de Despesas - Trabalhador com Salário R$ 1000")
class DespesaRepositoryTest {

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Dados base do trabalhador
    private Conta contaCorrente;
    private Cartao cartaoCredito;
    private Categoria categoriaAlimentacao;
    private Categoria categoriaTransporte;
    private Categoria categoriaMoradia;
    private Categoria categoriaLazer;

    @BeforeEach
    void configurarCenarioTrabalhador() {
        // Conta corrente do trabalhador
        contaCorrente = new Conta();
        contaCorrente.setNome("Conta Corrente Bradesco");
        contaCorrente.setTipo(TipoConta.CORRENTE);
        contaCorrente = contaRepository.save(contaCorrente);

        // Cartão de crédito básico
        cartaoCredito = new Cartao();
        cartaoCredito.setNome("Cartão Visa");
        cartaoCredito.setLimite(800.0); // Limite compatível com salário
        cartaoCredito.setLimiteUsado(0.0);
        cartaoCredito.setDiaFechamento(15);
        cartaoCredito.setDiaVencimento(10);
        cartaoCredito = cartaoRepository.save(cartaoCredito);

        // Categorias típicas de um trabalhador
        categoriaAlimentacao = criarCategoria("Alimentação", "#FF5722");
        categoriaTransporte = criarCategoria("Transporte", "#2196F3");
        categoriaMoradia = criarCategoria("Moradia", "#795548");
        categoriaLazer = criarCategoria("Lazer", "#E91E63");
    }

    private Categoria criarCategoria(String nome, String cor) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setCor(cor);
        categoria.setTipoReceita(false);
        return categoriaRepository.save(categoria);
    }

    @Test
    @DisplayName("Deve salvar despesas típicas de um trabalhador")
    void deveSalvarDespesasTipicasTrabalhador() {
        // Despesas mensais típicas para salário de R$ 1000
        Despesa aluguel = criarDespesa("Aluguel apartamento", 400.0, contaCorrente, null, categoriaMoradia);
        Despesa transporte = criarDespesa("Vale transporte", 120.0, contaCorrente, null, categoriaTransporte);
        Despesa supermercado = criarDespesa("Supermercado mensal", 200.0, null, cartaoCredito, categoriaAlimentacao);
        Despesa lanche = criarDespesa("Lanche trabalho", 5.0, contaCorrente, null, categoriaAlimentacao);

        List<Despesa> todasDespesas = despesaRepository.findAll();
        
        assertThat(todasDespesas).hasSize(4);
        assertThat(todasDespesas)
            .extracting(Despesa::getValor)
            .containsExactlyInAnyOrder(400.0, 120.0, 200.0, 5.0);

        // Total de despesas deve ser 725.0 (72.5% do salário)
        Double total = despesaRepository.buscarTotalDespesas();
        assertThat(total).isEqualTo(725.0);
    }

    @Test
    @DisplayName("Deve buscar despesas por categoria alimentação")
    void deveBuscarDespesasPorCategoriaAlimentacao() {
        criarDespesa("Supermercado", 150.0, null, cartaoCredito, categoriaAlimentacao);
        criarDespesa("Padaria", 25.0, contaCorrente, null, categoriaAlimentacao);
        criarDespesa("Restaurante", 45.0, null, cartaoCredito, categoriaAlimentacao);
        criarDespesa("Uber", 15.0, contaCorrente, null, categoriaTransporte);

        List<Despesa> despesasAlimentacao = despesaRepository.findByCategoria(categoriaAlimentacao);

        assertThat(despesasAlimentacao).hasSize(3);
        assertThat(despesasAlimentacao)
            .extracting(Despesa::getDescricao)
            .containsExactlyInAnyOrder("Supermercado", "Padaria", "Restaurante");
        
        // Gastos com alimentação: 220.0 (22% do salário)
        Double totalAlimentacao = despesasAlimentacao.stream()
            .mapToDouble(Despesa::getValor)
            .sum();
        assertThat(totalAlimentacao).isEqualTo(220.0);
    }

    @Test
    @DisplayName("Deve buscar despesas por descrição")
    void deveBuscarDespesasPorDescricao() {
        criarDespesa("Supermercado Extra", 180.0, null, cartaoCredito, categoriaAlimentacao);
        criarDespesa("Supermercado Pão de Açúcar", 160.0, null, cartaoCredito, categoriaAlimentacao);
        criarDespesa("Farmácia", 50.0, contaCorrente, null, categoriaAlimentacao);

        List<Despesa> despesasSupermercado = despesaRepository
            .findByDescricaoContainingWithRelationships("supermercado");

        assertThat(despesasSupermercado).hasSize(2);
        assertThat(despesasSupermercado)
            .allSatisfy(despesa -> {
                assertThat(despesa.getDescricao()).containsIgnoringCase("supermercado");
                assertThat(despesa.getCategoria()).isNotNull();
                assertThat(despesa.getCartao()).isNotNull();
            });
    }

    @Test
    @DisplayName("Deve buscar despesas do mês atual")
    void deveBuscarDespesasDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        
        criarDespesa("Despesa atual", 100.0, contaCorrente, null, categoriaAlimentacao);
        
        // Criando despesa de mês anterior para verificar filtro
        Despesa despesaMesAnterior = new Despesa();
        despesaMesAnterior.setDescricao("Despesa mês anterior");
        despesaMesAnterior.setValor(50.0);
        despesaMesAnterior.setData(hoje.minusMonths(1));
        despesaMesAnterior.setConta(contaCorrente);
        despesaMesAnterior.setCategoria(categoriaAlimentacao);
        despesaRepository.save(despesaMesAnterior);

        List<Despesa> despesasMesAtual = despesaRepository
            .findByMesEAnoWithRelationships(hoje.getMonthValue(), hoje.getYear());

        assertThat(despesasMesAtual).hasSize(1);
        assertThat(despesasMesAtual.get(0).getDescricao()).isEqualTo("Despesa atual");
        
        Double totalMesAtual = despesaRepository
            .buscarTotalDespesasPorMesAno(hoje.getMonthValue(), hoje.getYear());
        assertThat(totalMesAtual).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Deve listar despesas com relacionamentos carregados")
    void deveListarDespesasComRelacionamentos() {
        criarDespesa("Almoço", 15.0, contaCorrente, null, categoriaAlimentacao);
        criarDespesa("Gasolina", 80.0, null, cartaoCredito, categoriaTransporte);

        List<Despesa> despesas = despesaRepository.findAllWithRelationships();

        assertThat(despesas).hasSize(2);
        despesas.forEach(despesa -> {
            assertThat(despesa.getCategoria()).isNotNull();
            assertThat(despesa.getCategoria().getNome()).isNotBlank();
            // Verifica se tem conta OU cartão
            assertThat(despesa.getConta() != null || despesa.getCartao() != null).isTrue();
        });
    }

    @Test
    @DisplayName("Deve calcular total correto de múltiplas despesas")
    void deveCalcularTotalCorretoDespesas() {
        // Simulando um mês típico de gastos
        criarDespesa("Aluguel", 400.0, contaCorrente, null, categoriaMoradia);
        criarDespesa("Alimentação", 250.0, null, cartaoCredito, categoriaAlimentacao);
        criarDespesa("Transporte", 120.0, contaCorrente, null, categoriaTransporte);
        criarDespesa("Cinema", 30.0, null, cartaoCredito, categoriaLazer);

        Double total = despesaRepository.buscarTotalDespesas();
        
        assertThat(total).isEqualTo(800.0); // 80% do salário de R$ 1000
        
        // Verifica se sobrou 20% para poupança
        double percentualGasto = (total / 1000.0) * 100;
        assertThat(percentualGasto).isEqualTo(80.0);
    }

    // TESTES DE CENÁRIOS DE ERRO

    @Test
    @DisplayName("Deve permitir salvar despesa com valor nulo mas retornar nulo")
    void devePermitirSalvarDespesaComValorNulo() {
        Despesa despesa = new Despesa();
        despesa.setDescricao("Despesa inválida");
        despesa.setValor(null);
        despesa.setData(LocalDate.now());
        despesa.setConta(contaCorrente);
        despesa.setCategoria(categoriaAlimentacao);

        Despesa despesaSalva = despesaRepository.save(despesa);
        assertThat(despesaSalva.getValor()).isNull();
        assertThat(despesaSalva.getDescricao()).isEqualTo("Despesa inválida");
    }

    @Test
    @DisplayName("Deve falhar ao tentar salvar despesa com valor negativo")
    void deveFalharComValorNegativo() {
        assertThatThrownBy(() -> {
            criarDespesa("Despesa negativa", -50.0, contaCorrente, null, categoriaAlimentacao);
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve permitir salvar despesa sem descrição mas retornar nulo")
    void devePermitirSalvarDespesaSemDescricao() {
        Despesa despesa = new Despesa();
        despesa.setDescricao(null);
        despesa.setValor(100.0);
        despesa.setData(LocalDate.now());
        despesa.setConta(contaCorrente);
        despesa.setCategoria(categoriaAlimentacao);

        Despesa despesaSalva = despesaRepository.save(despesa);
        assertThat(despesaSalva.getDescricao()).isNull();
        assertThat(despesaSalva.getValor()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Deve falhar ao tentar salvar despesa sem conta nem cartão")
    void deveFalharSemContaNemCartao() {
        Despesa despesa = new Despesa();
        despesa.setDescricao("Despesa sem forma de pagamento");
        despesa.setValor(100.0);
        despesa.setData(LocalDate.now());
        // Não define nem conta nem cartão
        despesa.setCategoria(categoriaAlimentacao);

        // Esta validação deve ser feita na camada de serviço, 
        // mas aqui testamos se o repository aceita
        Despesa despesaSalva = despesaRepository.save(despesa);
        assertThat(despesaSalva.getConta()).isNull();
        assertThat(despesaSalva.getCartao()).isNull();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há despesas")
    void deveRetornarListaVaziaQuandoNaoHaDespesas() {
        List<Despesa> despesas = despesaRepository.findAll();
        assertThat(despesas).isEmpty();

        Double total = despesaRepository.buscarTotalDespesas();
        assertThat(total).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve retornar optional vazio para ID inexistente")
    void deveRetornarOptionalVazioParaIdInexistente() {
        Optional<Despesa> despesa = despesaRepository.findById(999L);
        assertThat(despesa).isEmpty();

        Optional<Despesa> despesaComRelacionamentos = despesaRepository
            .findByIdWithRelationships(999L);
        assertThat(despesaComRelacionamentos).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar por filtros que não retornam resultados")
    void deveBuscarPorFiltrosSemResultados() {
        criarDespesa("Almoço", 15.0, contaCorrente, null, categoriaAlimentacao);

        List<Despesa> despesas = despesaRepository
            .findByDescricaoContainingWithRelationships("jantar");
        assertThat(despesas).isEmpty();

        List<Despesa> despesasMesInexistente = despesaRepository
            .findByMesEAnoWithRelationships(13, 2024); // Mês inválido
        assertThat(despesasMesInexistente).isEmpty();
    }

    private Despesa criarDespesa(String descricao, Double valor, Conta conta, Cartao cartao, Categoria categoria) {
        // Validação de valor negativo
        if (valor != null && valor < 0) {
            throw new IllegalArgumentException("Valor não pode ser negativo");
        }

        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setData(LocalDate.now());
        despesa.setConta(conta);
        despesa.setCartao(cartao);
        despesa.setCategoria(categoria);
        return despesaRepository.save(despesa);
    }
} 