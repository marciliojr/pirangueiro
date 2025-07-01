package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes BDD dos Repositories de Despesa e Receita - Cenário do Trabalhador João")
class DespesaReceitaRepositoryBDDTest {

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Contexto do trabalhador João
    private Conta contaSalario;
    private Conta poupanca;
    private Cartao cartaoBasico;
    private Categoria categoriaAlimentacao;
    private Categoria categoriaTransporte;
    private Categoria categoriaSalario;
    private Categoria categoriaFreelance;

    @BeforeEach
    void configurarCenarioTrabalhadorJoao() {
        // Dado que existe um trabalhador chamado "João"
        
        // E ele possui uma conta corrente "Conta Salário"
        contaSalario = new Conta();
        contaSalario.setNome("Conta Salário");
        contaSalario.setTipo(TipoConta.CORRENTE);
        contaSalario = contaRepository.save(contaSalario);

        // E ele possui uma conta poupança "Poupança"
        poupanca = new Conta();
        poupanca.setNome("Poupança");
        poupanca.setTipo(TipoConta.POUPANCA);
        poupanca = contaRepository.save(poupanca);

        // E ele possui um cartão de crédito "Cartão Básico" com limite de R$ 500
        cartaoBasico = new Cartao();
        cartaoBasico.setNome("Cartão Básico");
        cartaoBasico.setLimite(500.0);
        cartaoBasico.setLimiteUsado(0.0);
        cartaoBasico.setDiaFechamento(10);
        cartaoBasico.setDiaVencimento(15);
        cartaoBasico = cartaoRepository.save(cartaoBasico);

        // E existem categorias de despesa
        categoriaAlimentacao = criarCategoria("Alimentação", "#FF5722", false);
        categoriaTransporte = criarCategoria("Transporte", "#2196F3", false);

        // E existem categorias de receita
        categoriaSalario = criarCategoria("Salário", "#4CAF50", true);
        categoriaFreelance = criarCategoria("Freelance", "#FF9800", true);
    }

    private Categoria criarCategoria(String nome, String cor, Boolean tipoReceita) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setCor(cor);
        categoria.setTipoReceita(tipoReceita);
        return categoriaRepository.save(categoria);
    }

    @Test
    @DisplayName("Cenário Positivo: Trabalhador com despesas básicas mensais e renda regular")
    void cenarioPositivoTrabalhadorComDespesasBasicasERenda() {
        // Quando João registra receitas no mês atual
        Receita salario = criarReceita("Salário CLT", 1000.0, contaSalario, categoriaSalario);
        Receita freelance = criarReceita("Freelance programação", 300.0, contaSalario, categoriaFreelance);

        // E João registra despesas no mês atual
        Despesa almoco = criarDespesa("Almoço no trabalho", 15.0, contaSalario, null, categoriaAlimentacao);
        Despesa transporte = criarDespesa("Passagem de ônibus", 4.50, contaSalario, null, categoriaTransporte);
        Despesa supermercado = criarDespesa("Supermercado", 120.0, null, cartaoBasico, categoriaAlimentacao);

        // Então devo conseguir buscar todas as receitas e despesas com relacionamentos
        List<Receita> receitas = receitaRepository.findAllWithRelationships();
        List<Despesa> despesas = despesaRepository.findAllWithRelationships();

        // E devo encontrar as quantidades corretas
        assertThat(receitas).hasSize(2);
        assertThat(despesas).hasSize(3);

        // E os totais devem estar corretos
        Double totalReceitas = receitaRepository.buscarTotalReceitas();
        Double totalDespesas = despesaRepository.buscarTotalDespesas();

        assertThat(totalReceitas).isEqualTo(1300.0);
        assertThat(totalDespesas).isEqualTo(139.5);

        // E deve ter saldo positivo (receitas > despesas)
        assertThat(totalReceitas).isGreaterThan(totalDespesas);

        // E devo conseguir buscar por categoria
        List<Despesa> despesasAlimentacao = despesaRepository.findByCategoria(categoriaAlimentacao);
        assertThat(despesasAlimentacao).hasSize(2); // almoço + supermercado

        List<Receita> receitasSalario = receitaRepository.findByCategoria(categoriaSalario);
        assertThat(receitasSalario).hasSize(1);
    }

    @Test
    @DisplayName("Cenário Negativo: Trabalhador com baixa renda e gastos excessivos")
    void cenarioNegativoTrabalhadorComBaixaRendaEGastosExcessivos() {
        // Quando João registra apenas receitas mínimas
        Receita trabalhoEsporadico = criarReceita("Trabalho esporádico", 200.0, contaSalario, categoriaFreelance);
        Receita ajudaFamiliar = criarReceita("Ajuda familiar", 150.0, contaSalario, categoriaSalario);

        // E registra despesas excessivas
        Despesa contaCartao = criarDespesa("Conta de cartão", 800.0, contaSalario, null, categoriaAlimentacao);
        Despesa financiamentoCarro = criarDespesa("Financiamento carro", 350.0, contaSalario, null, categoriaTransporte);

        // Então deve conseguir cadastrar tudo
        List<Receita> receitas = receitaRepository.findAll();
        List<Despesa> despesas = despesaRepository.findAll();

        assertThat(receitas).hasSize(2);
        assertThat(despesas).hasSize(2);

        // E os totais devem refletir a situação negativa
        Double totalReceitas = receitaRepository.buscarTotalReceitas();
        Double totalDespesas = despesaRepository.buscarTotalDespesas();

        assertThat(totalReceitas).isEqualTo(350.0);
        assertThat(totalDespesas).isEqualTo(1150.0);

        // E deve ter saldo negativo (despesas > receitas)
        assertThat(totalDespesas).isGreaterThan(totalReceitas);

        // E as consultas devem funcionar normalmente mesmo com valores altos
        Double totalMensal = despesaRepository.buscarTotalDespesasPorMesAno(
            LocalDate.now().getMonthValue(), 
            LocalDate.now().getYear()
        );
        assertThat(totalMensal).isEqualTo(totalDespesas);
    }

    @Test
    @DisplayName("Busca e filtros funcionando corretamente")
    void testeBuscaEFiltrosFuncionandoCorretamente() {
        // Dado que João tem despesas e receitas variadas
        criarReceita("Salário dezembro", 1000.0, contaSalario, categoriaSalario);
        criarDespesa("Almoço restaurante", 25.0, contaSalario, null, categoriaAlimentacao);
        criarDespesa("Uber para casa", 15.0, contaSalario, null, categoriaTransporte);

        // Quando busco por descrição
        List<Despesa> despesasAlmoco = despesaRepository.findByDescricaoContainingWithRelationships("almoço");
        List<Receita> receitasSalario = receitaRepository.findByDescricaoContainingWithRelationships("salário");

        // Então deve encontrar as descrições corretas
        assertThat(despesasAlmoco).hasSize(1);
        assertThat(despesasAlmoco.get(0).getDescricao()).containsIgnoringCase("almoço");

        assertThat(receitasSalario).hasSize(1);
        assertThat(receitasSalario.get(0).getDescricao()).containsIgnoringCase("salário");

        // E os relacionamentos devem estar carregados
        assertThat(despesasAlmoco.get(0).getCategoria()).isNotNull();
        assertThat(despesasAlmoco.get(0).getConta()).isNotNull();

        assertThat(receitasSalario.get(0).getCategoria()).isNotNull();
        assertThat(receitasSalario.get(0).getConta()).isNotNull();

        // Quando busco por mês e ano
        LocalDate hoje = LocalDate.now();
        List<Despesa> despesasMesAtual = despesaRepository.findByMesEAnoWithRelationships(
            hoje.getMonthValue(), hoje.getYear()
        );
        List<Receita> receitasMesAtual = receitaRepository.findByMesEAnoWithRelationships(
            hoje.getMonthValue(), hoje.getYear()
        );

        // Então deve encontrar apenas do mês atual
        assertThat(despesasMesAtual).hasSize(2);
        assertThat(receitasMesAtual).hasSize(1);

        for (Despesa despesa : despesasMesAtual) {
            assertThat(despesa.getData().getMonthValue()).isEqualTo(hoje.getMonthValue());
            assertThat(despesa.getData().getYear()).isEqualTo(hoje.getYear());
        }
    }

    private Receita criarReceita(String descricao, Double valor, Conta conta, Categoria categoria) {
        Receita receita = new Receita();
        receita.setDescricao(descricao);
        receita.setValor(valor);
        receita.setData(LocalDate.now());
        receita.setConta(conta);
        receita.setCategoria(categoria);
        return receitaRepository.save(receita);
    }

    private Despesa criarDespesa(String descricao, Double valor, Conta conta, Cartao cartao, Categoria categoria) {
        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setData(LocalDate.now());
        despesa.setConta(conta);
        despesa.setCartao(cartao);
        despesa.setCategoria(categoria);
        // Não define o campo 'pago' conforme solicitado
        return despesaRepository.save(despesa);
    }
} 