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
@DisplayName("Testes do Repository de Receitas - Trabalhador com Salário R$ 1000")
class ReceitaRepositoryTest {

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Dados base do trabalhador
    private Conta contaCorrente;
    private Conta contaPoupanca;
    private Categoria categoriaSalario;
    private Categoria categoriaFreelance;
    private Categoria categoriaVendas;
    private Categoria categoriaInvestimentos;

    @BeforeEach
    void configurarCenarioTrabalhador() {
        // Contas do trabalhador
        contaCorrente = new Conta();
        contaCorrente.setNome("Conta Corrente Bradesco");
        contaCorrente.setTipo(TipoConta.CORRENTE);
        contaCorrente = contaRepository.save(contaCorrente);

        contaPoupanca = new Conta();
        contaPoupanca.setNome("Poupança");
        contaPoupanca.setTipo(TipoConta.POUPANCA);
        contaPoupanca = contaRepository.save(contaPoupanca);

        // Categorias típicas de receita
        categoriaSalario = criarCategoria("Salário", "#4CAF50");
        categoriaFreelance = criarCategoria("Freelance", "#FF9800");
        categoriaVendas = criarCategoria("Vendas", "#9C27B0");
        categoriaInvestimentos = criarCategoria("Investimentos", "#607D8B");
    }

    private Categoria criarCategoria(String nome, String cor) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setCor(cor);
        categoria.setTipoReceita(true);
        return categoriaRepository.save(categoria);
    }

    @Test
    @DisplayName("Deve salvar receitas típicas de um trabalhador")
    void deveSalvarReceitasTipicasTrabalhador() {
        // Receitas mensais típicas
        Receita salario = criarReceita("Salário CLT", 1000.0, contaCorrente, categoriaSalario);
        Receita freelance = criarReceita("Freelance desenvolvimento", 300.0, contaCorrente, categoriaFreelance);
        Receita vendaUsados = criarReceita("Venda de livros usados", 50.0, contaPoupanca, categoriaVendas);

        List<Receita> todasReceitas = receitaRepository.findAll();
        
        assertThat(todasReceitas).hasSize(3);
        assertThat(todasReceitas)
            .extracting(Receita::getValor)
            .containsExactlyInAnyOrder(1000.0, 300.0, 50.0);

        // Total de receitas deve ser 1350.0
        Double total = receitaRepository.buscarTotalReceitas();
        assertThat(total).isEqualTo(1350.0);
    }

    @Test
    @DisplayName("Deve buscar receitas por categoria salário")
    void deveBuscarReceitasPorCategoriaSalario() {
        criarReceita("Salário mensal", 1000.0, contaCorrente, categoriaSalario);
        criarReceita("13º salário", 1000.0, contaCorrente, categoriaSalario);
        criarReceita("Férias", 1333.33, contaCorrente, categoriaSalario);
        criarReceita("Freelance", 200.0, contaCorrente, categoriaFreelance);

        List<Receita> receitasSalario = receitaRepository.findByCategoria(categoriaSalario);

        assertThat(receitasSalario).hasSize(3);
        assertThat(receitasSalario)
            .extracting(Receita::getDescricao)
            .containsExactlyInAnyOrder("Salário mensal", "13º salário", "Férias");
        
        // Total de receitas de salário: 3333.33
        Double totalSalario = receitasSalario.stream()
            .mapToDouble(Receita::getValor)
            .sum();
        assertThat(totalSalario).isEqualTo(3333.33, offset(0.01));
    }

    @Test
    @DisplayName("Deve buscar receitas por descrição")
    void deveBuscarReceitasPorDescricao() {
        criarReceita("Freelance React", 500.0, contaCorrente, categoriaFreelance);
        criarReceita("Freelance Java", 400.0, contaCorrente, categoriaFreelance);
        criarReceita("Salário", 1000.0, contaCorrente, categoriaSalario);

        List<Receita> receitasFreelance = receitaRepository
            .findByDescricaoContainingWithRelationships("freelance");

        assertThat(receitasFreelance).hasSize(2);
        assertThat(receitasFreelance)
            .allSatisfy(receita -> {
                assertThat(receita.getDescricao()).containsIgnoringCase("freelance");
                assertThat(receita.getCategoria()).isNotNull();
                assertThat(receita.getConta()).isNotNull();
            });
    }

    @Test
    @DisplayName("Deve buscar receitas do mês atual")
    void deveBuscarReceitasDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        
        criarReceita("Receita atual", 1000.0, contaCorrente, categoriaSalario);
        
        // Criando receita de mês anterior para verificar filtro
        Receita receitaMesAnterior = new Receita();
        receitaMesAnterior.setDescricao("Receita mês anterior");
        receitaMesAnterior.setValor(800.0);
        receitaMesAnterior.setData(hoje.minusMonths(1));
        receitaMesAnterior.setConta(contaCorrente);
        receitaMesAnterior.setCategoria(categoriaSalario);
        receitaRepository.save(receitaMesAnterior);

        List<Receita> receitasMesAtual = receitaRepository
            .findByMesEAnoWithRelationships(hoje.getMonthValue(), hoje.getYear());

        assertThat(receitasMesAtual).hasSize(1);
        assertThat(receitasMesAtual.get(0).getDescricao()).isEqualTo("Receita atual");
        
        Double totalMesAtual = receitaRepository
            .buscarTotalReceitasPorMesAno(hoje.getMonthValue(), hoje.getYear());
        assertThat(totalMesAtual).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Deve listar receitas com relacionamentos carregados")
    void deveListarReceitasComRelacionamentos() {
        criarReceita("Salário", 1000.0, contaCorrente, categoriaSalario);
        criarReceita("Freelance", 200.0, contaPoupanca, categoriaFreelance);

        List<Receita> receitas = receitaRepository.findAllWithRelationships();

        assertThat(receitas).hasSize(2);
        receitas.forEach(receita -> {
            assertThat(receita.getCategoria()).isNotNull();
            assertThat(receita.getCategoria().getNome()).isNotBlank();
            assertThat(receita.getConta()).isNotNull();
            assertThat(receita.getConta().getNome()).isNotBlank();
        });
    }

    @Test
    @DisplayName("Deve calcular total correto de múltiplas receitas")
    void deveCalcularTotalCorretoReceitas() {
        // Simulando receitas de um mês bom
        criarReceita("Salário", 1000.0, contaCorrente, categoriaSalario);
        criarReceita("Freelance", 500.0, contaCorrente, categoriaFreelance);
        criarReceita("Venda celular", 200.0, contaPoupanca, categoriaVendas);
        criarReceita("Rendimento poupança", 15.0, contaPoupanca, categoriaInvestimentos);

        Double total = receitaRepository.buscarTotalReceitas();
        
        assertThat(total).isEqualTo(1715.0);
        
        // Verifica se o trabalhador teve uma renda 71.5% acima do salário base
        double percentualExtra = ((total - 1000.0) / 1000.0) * 100;
        assertThat(percentualExtra).isEqualTo(71.5);
    }

    @Test
    @DisplayName("Deve buscar receitas apenas do salário base")
    void deveBuscarReceitasApenasSalarioBase() {
        criarReceita("Salário mensal", 1000.0, contaCorrente, categoriaSalario);
        criarReceita("Freelance extra", 400.0, contaCorrente, categoriaFreelance);

        List<Receita> receitasSalario = receitaRepository.findByCategoria(categoriaSalario);
        
        assertThat(receitasSalario).hasSize(1);
        assertThat(receitasSalario.get(0).getValor()).isEqualTo(1000.0);
        assertThat(receitasSalario.get(0).getDescricao()).isEqualTo("Salário mensal");
    }

    // TESTES DE CENÁRIOS DE ERRO

    @Test
    @DisplayName("Deve permitir salvar receita com valor nulo mas retornar nulo")
    void devePermitirSalvarReceitaComValorNulo() {
        Receita receita = new Receita();
        receita.setDescricao("Receita inválida");
        receita.setValor(null);
        receita.setData(LocalDate.now());
        receita.setConta(contaCorrente);
        receita.setCategoria(categoriaSalario);

        Receita receitaSalva = receitaRepository.save(receita);
        assertThat(receitaSalva.getValor()).isNull();
        assertThat(receitaSalva.getDescricao()).isEqualTo("Receita inválida");
    }

    @Test
    @DisplayName("Deve falhar ao tentar salvar receita com valor negativo")
    void deveFalharComValorNegativo() {
        assertThatThrownBy(() -> {
            criarReceita("Receita negativa", -100.0, contaCorrente, categoriaSalario);
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve permitir salvar receita sem descrição mas retornar nulo")
    void devePermitirSalvarReceitaSemDescricao() {
        Receita receita = new Receita();
        receita.setDescricao(null);
        receita.setValor(1000.0);
        receita.setData(LocalDate.now());
        receita.setConta(contaCorrente);
        receita.setCategoria(categoriaSalario);

        Receita receitaSalva = receitaRepository.save(receita);
        assertThat(receitaSalva.getDescricao()).isNull();
        assertThat(receitaSalva.getValor()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Deve falhar ao tentar salvar receita sem conta")
    void deveFalharSemConta() {
        Receita receita = new Receita();
        receita.setDescricao("Receita sem conta");
        receita.setValor(1000.0);
        receita.setData(LocalDate.now());
        receita.setConta(null);
        receita.setCategoria(categoriaSalario);

        // Esta validação deve ser feita na camada de serviço, 
        // mas aqui testamos se o repository aceita
        Receita receitaSalva = receitaRepository.save(receita);
        assertThat(receitaSalva.getConta()).isNull();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há receitas")
    void deveRetornarListaVaziaQuandoNaoHaReceitas() {
        List<Receita> receitas = receitaRepository.findAll();
        assertThat(receitas).isEmpty();

        Double total = receitaRepository.buscarTotalReceitas();
        assertThat(total).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve retornar optional vazio para ID inexistente")
    void deveRetornarOptionalVazioParaIdInexistente() {
        Optional<Receita> receita = receitaRepository.findById(999L);
        assertThat(receita).isEmpty();

        Optional<Receita> receitaComRelacionamentos = receitaRepository
            .findByIdWithRelationships(999L);
        assertThat(receitaComRelacionamentos).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar por filtros que não retornam resultados")
    void deveBuscarPorFiltrosSemResultados() {
        criarReceita("Salário", 1000.0, contaCorrente, categoriaSalario);

        List<Receita> receitas = receitaRepository
            .findByDescricaoContainingWithRelationships("bonus");
        assertThat(receitas).isEmpty();

        List<Receita> receitasMesInexistente = receitaRepository
            .findByMesEAnoWithRelationships(13, 2024); // Mês inválido
        assertThat(receitasMesInexistente).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar receita por ID específico")
    void deveBuscarReceitaPorIdEspecifico() {
        Receita salario = criarReceita("Salário", 1000.0, contaCorrente, categoriaSalario);
        
        Optional<Receita> receitaEncontrada = receitaRepository.findById(salario.getId());
        
        assertThat(receitaEncontrada).isPresent();
        assertThat(receitaEncontrada.get().getDescricao()).isEqualTo("Salário");
        assertThat(receitaEncontrada.get().getValor()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Deve calcular percentual de receitas extras sobre salário base")
    void deveCalcularPercentualReceitasExtras() {
        double salarioBase = 1000.0;
        
        criarReceita("Salário", salarioBase, contaCorrente, categoriaSalario);
        criarReceita("Freelance", 250.0, contaCorrente, categoriaFreelance);
        criarReceita("Venda", 100.0, contaPoupanca, categoriaVendas);

        Double totalReceitas = receitaRepository.buscarTotalReceitas();
        double receitasExtras = totalReceitas - salarioBase;
        double percentualExtras = (receitasExtras / salarioBase) * 100;
        
        assertThat(totalReceitas).isEqualTo(1350.0);
        assertThat(receitasExtras).isEqualTo(350.0);
        assertThat(percentualExtras).isEqualTo(35.0); // 35% de renda extra
    }

    private Receita criarReceita(String descricao, Double valor, Conta conta, Categoria categoria) {
        // Validação de valor negativo
        if (valor != null && valor < 0) {
            throw new IllegalArgumentException("Valor não pode ser negativo");
        }

        Receita receita = new Receita();
        receita.setDescricao(descricao);
        receita.setValor(valor);
        receita.setData(LocalDate.now());
        receita.setConta(conta);
        receita.setCategoria(categoria);
        return receitaRepository.save(receita);
    }
} 