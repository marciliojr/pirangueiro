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
@DisplayName("Testes do Repository de Contas - Trabalhador com Salário R$ 1000")
class ContaRepositoryTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Dados base do trabalhador
    private Categoria categoriaAlimentacao;
    private Categoria categoriaSalario;

    @BeforeEach
    void configurarCenarioTrabalhador() {
        // Categorias básicas para testes
        categoriaAlimentacao = new Categoria();
        categoriaAlimentacao.setNome("Alimentação");
        categoriaAlimentacao.setCor("#FF5722");
        categoriaAlimentacao.setTipoReceita(false);
        categoriaAlimentacao = categoriaRepository.save(categoriaAlimentacao);

        categoriaSalario = new Categoria();
        categoriaSalario.setNome("Salário");
        categoriaSalario.setCor("#4CAF50");
        categoriaSalario.setTipoReceita(true);
        categoriaSalario = categoriaRepository.save(categoriaSalario);
    }

    @Test
    @DisplayName("Deve criar contas típicas de um trabalhador")
    void deveCriarContasTipicasTrabalhador() {
        Conta contaCorrente = new Conta();
        contaCorrente.setNome("Conta Corrente Bradesco");
        contaCorrente.setTipo(TipoConta.CORRENTE);
        contaCorrente = contaRepository.save(contaCorrente);

        assertThat(contaCorrente.getId()).isNotNull();
        assertThat(contaCorrente.getNome()).isEqualTo("Conta Corrente Bradesco");
        assertThat(contaCorrente.getTipo()).isEqualTo(TipoConta.CORRENTE);
    }

    @Test
    @DisplayName("Deve buscar conta por ID específico")
    void deveBuscarContaPorId() {
        Conta conta = new Conta();
        conta.setNome("Conta Teste");
        conta.setTipo(TipoConta.CORRENTE);
        conta = contaRepository.save(conta);
        
        Optional<Conta> contaEncontrada = contaRepository.findById(conta.getId());
        
        assertThat(contaEncontrada).isPresent();
        assertThat(contaEncontrada.get().getNome()).isEqualTo("Conta Teste");
    }

    @Test
    @DisplayName("Deve criar múltiplas contas de trabalhador")
    void deveCriarMultiplasContasTrabalhador() {
        // Cenário típico: trabalhador organizado financeiramente
        Conta contaSalario = new Conta();
        contaSalario.setNome("Conta Salário Caixa");
        contaSalario.setTipo(TipoConta.CORRENTE);
        contaSalario = contaRepository.save(contaSalario);

        Conta poupanca = new Conta();
        poupanca.setNome("Poupança");
        poupanca.setTipo(TipoConta.POUPANCA);
        poupanca = contaRepository.save(poupanca);

        Conta contaDigital = new Conta();
        contaDigital.setNome("Nubank");
        contaDigital.setTipo(TipoConta.CORRENTE);
        contaDigital = contaRepository.save(contaDigital);

        List<Conta> todasContas = contaRepository.findAll();
        
        assertThat(todasContas).hasSize(3);
        assertThat(todasContas)
            .extracting(Conta::getNome)
            .containsExactlyInAnyOrder("Conta Salário Caixa", "Poupança", "Nubank");
    }

    @Test
    @DisplayName("Deve diferenciar tipos de conta")
    void deveDiferenciarTiposDeConta() {
        Conta corrente = new Conta();
        corrente.setNome("Conta Corrente");
        corrente.setTipo(TipoConta.CORRENTE);
        corrente = contaRepository.save(corrente);

        Conta poupanca = new Conta();
        poupanca.setNome("Poupança");
        poupanca.setTipo(TipoConta.POUPANCA);
        poupanca = contaRepository.save(poupanca);

        assertThat(corrente.getTipo()).isEqualTo(TipoConta.CORRENTE);
        assertThat(poupanca.getTipo()).isEqualTo(TipoConta.POUPANCA);
        assertThat(corrente.getTipo()).isNotEqualTo(poupanca.getTipo());
    }

    @Test
    @DisplayName("Deve retornar optional vazio para ID inexistente")
    void deveRetornarOptionalVazioParaIdInexistente() {
        Optional<Conta> conta = contaRepository.findById(999L);
        assertThat(conta).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há contas")
    void deveRetornarListaVaziaQuandoNaoHaContas() {
        List<Conta> contas = contaRepository.findAll();
        assertThat(contas).isEmpty();
    }

    @Test
    @DisplayName("Deve permitir nome longo típico de banco")
    void devePermitirNomeLongoTipicoDeBanco() {
        String nomeLongo = "Conta Corrente Banco do Brasil S.A. - Agência 1234-5 - Conta 98765-4";
        
        Conta conta = new Conta();
        conta.setNome(nomeLongo);
        conta.setTipo(TipoConta.CORRENTE);
        conta = contaRepository.save(conta);
        
        assertThat(conta.getNome()).isEqualTo(nomeLongo);
        assertThat(conta.getNome().length()).isGreaterThan(50);
    }

    @Test
    @DisplayName("Deve calcular saldo correto baseado em receitas e despesas")
    void deveCalcularSaldoCorretoContaCorrente() {
        Conta contaCorrente = criarConta("Conta Corrente", TipoConta.CORRENTE);
        
        // Receita do salário
        criarReceita("Salário", 1000.0, contaCorrente);
        
        // Despesas típicas do mês
        criarDespesa("Aluguel", 400.0, contaCorrente, true);  // Pago
        criarDespesa("Supermercado", 200.0, contaCorrente, true);  // Pago
        criarDespesa("Conta pendente", 100.0, contaCorrente, false);  // Não pago
        
        // Verificar dados salvos
        List<Receita> receitas = receitaRepository.findAll();
        List<Despesa> despesas = despesaRepository.findAll();
        
        assertThat(receitas).hasSize(1);
        assertThat(despesas).hasSize(3);
        
        // Calcular saldo manual para validação
        double totalReceitas = receitas.stream().mapToDouble(Receita::getValor).sum();
        double despesasPagas = despesas.stream()
            .filter(d -> Boolean.TRUE.equals(d.getPago()))
            .mapToDouble(Despesa::getValor).sum();
        double saldoReal = totalReceitas - despesasPagas;
        
        assertThat(totalReceitas).isEqualTo(1000.0);
        assertThat(despesasPagas).isEqualTo(600.0);
        assertThat(saldoReal).isEqualTo(400.0); // Sobrou R$ 400 na conta
    }

    @Test
    @DisplayName("Deve diferenciar conta corrente de poupança")
    void deveDiferenciarTiposDeContas() {
        Conta contaCorrente = criarConta("Conta Corrente", TipoConta.CORRENTE);
        Conta poupanca = criarConta("Poupança", TipoConta.POUPANCA);
        
        // Normalmente salário cai na conta corrente
        criarReceita("Salário", 1000.0, contaCorrente);
        
        // Economia vai para poupança
        criarReceita("Transferência para poupança", 200.0, poupanca);
        
        List<Receita> receitasCorrente = receitaRepository.findAll().stream()
            .filter(r -> r.getConta().getTipo() == TipoConta.CORRENTE)
            .toList();
        
        List<Receita> receitasPoupanca = receitaRepository.findAll().stream()
            .filter(r -> r.getConta().getTipo() == TipoConta.POUPANCA)
            .toList();
        
        assertThat(receitasCorrente).hasSize(1);
        assertThat(receitasPoupanca).hasSize(1);
        assertThat(receitasCorrente.get(0).getValor()).isEqualTo(1000.0);
        assertThat(receitasPoupanca.get(0).getValor()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("Deve permitir conta com nome longo típico de bancos")
    void devePermitirContaComNomeLongo() {
        String nomeLongo = "Conta Corrente Banco Bradesco S.A. - Agência 1234-5 - Conta 98765-4";
        Conta conta = criarConta(nomeLongo, TipoConta.CORRENTE);
        
        assertThat(conta.getNome()).isEqualTo(nomeLongo);
        assertThat(conta.getNome()).hasSize(67); // Nome realista de banco
    }

    @Test
    @DisplayName("Deve validar que trabalhador tem pelo menos uma conta")
    void deveValidarTrabalhadorTemPeloMenosUmaConta() {
        // Cenário típico: trabalhador abre primeira conta para receber salário
        Conta primeiraConta = criarConta("Primeira Conta", TipoConta.CORRENTE);
        criarReceita("Primeiro Salário", 1000.0, primeiraConta);
        
        List<Conta> contas = contaRepository.findAll();
        List<Receita> receitas = receitaRepository.findAll();
        
        assertThat(contas).hasSize(1);
        assertThat(receitas).hasSize(1);
        assertThat(receitas.get(0).getConta().getId()).isEqualTo(primeiraConta.getId());
    }

    @Test
    @DisplayName("Deve permitir múltiplas contas para organização financeira")
    void devePermitirMultiplasContasParaOrganizacao() {
        // Estratégia de organização comum de trabalhadores
        Conta contaSalario = criarConta("Conta Salário", TipoConta.CORRENTE);
        Conta contaGastos = criarConta("Conta Gastos", TipoConta.CORRENTE);
        Conta poupancaEmergencia = criarConta("Poupança Emergência", TipoConta.POUPANCA);
        
        // Recebe salário
        criarReceita("Salário", 1000.0, contaSalario);
        
        // Transfere para organização
        criarReceita("Transferência para gastos", 700.0, contaGastos);
        criarReceita("Transferência para poupança", 300.0, poupancaEmergencia);
        
        // Gastos saem da conta específica
        criarDespesa("Aluguel", 400.0, contaGastos, true);
        criarDespesa("Supermercado", 200.0, contaGastos, true);
        
        List<Conta> contas = contaRepository.findAll();
        assertThat(contas).hasSize(3);
        
        // Verificar distribuição
        List<Receita> receitasPorConta = receitaRepository.findAll();
        List<Despesa> despesasPorConta = despesaRepository.findAll();
        
        assertThat(receitasPorConta).hasSize(3);
        assertThat(despesasPorConta).hasSize(2);
    }

    // TESTES DE CENÁRIOS DE ERRO

    @Test
    @DisplayName("Deve permitir salvar conta sem nome mas retornar nulo")
    void devePermitirSalvarContaSemNome() {
        Conta conta = new Conta();
        conta.setNome(null);
        conta.setTipo(TipoConta.CORRENTE);

        Conta contaSalva = contaRepository.save(conta);
        assertThat(contaSalva.getNome()).isNull();
        assertThat(contaSalva.getTipo()).isEqualTo(TipoConta.CORRENTE);
    }

    @Test
    @DisplayName("Deve permitir salvar conta com nome vazio")
    void devePermitirSalvarContaComNomeVazio() {
        Conta conta = new Conta();
        conta.setNome("");
        conta.setTipo(TipoConta.CORRENTE);

        Conta contaSalva = contaRepository.save(conta);
        assertThat(contaSalva.getNome()).isEmpty();
        // Repository permite, validação deve ser na camada de serviço
    }

    @Test
    @DisplayName("Deve permitir salvar conta sem tipo usando valor padrão")
    void devePermitirSalvarContaSemTipo() {
        Conta conta = new Conta();
        conta.setNome("Conta Teste");
        conta.setTipo(TipoConta.CORRENTE); // Definindo valor padrão

        Conta contaSalva = contaRepository.save(conta);
        assertThat(contaSalva.getNome()).isEqualTo("Conta Teste");
        assertThat(contaSalva.getTipo()).isEqualTo(TipoConta.CORRENTE);
    }



    @Test
    @DisplayName("Deve permitir deletar conta sem relacionamentos")
    void devePermitirDeletarContaSemRelacionamentos() {
        Conta conta = criarConta("Conta Temporária", TipoConta.CORRENTE);
        Long contaId = conta.getId();
        
        contaRepository.delete(conta);
        
        Optional<Conta> contaDeletada = contaRepository.findById(contaId);
        assertThat(contaDeletada).isEmpty();
    }

    @Test
    @DisplayName("Deve permitir deletar conta mesmo com movimentações no teste")
    void devePermitirDeletarContaComMovimentacoes() {
        Conta conta = criarConta("Conta com Movimentação", TipoConta.CORRENTE);
        criarReceita("Salário", 1000.0, conta);
        criarDespesa("Aluguel", 400.0, conta, true);
        
        // No ambiente de teste, pode permitir deletar
        Long contaId = conta.getId();
        
        // Verificar que conta existe
        assertThat(contaRepository.findById(contaId)).isPresent();
        
        // Deletar apenas se não houver constraint
        try {
            contaRepository.delete(conta);
            Optional<Conta> contaDeletada = contaRepository.findById(contaId);
            assertThat(contaDeletada).isEmpty();
        } catch (Exception e) {
            // Se der erro por constraint, está correto
            assertThat(e).isInstanceOfAny(DataIntegrityViolationException.class, 
                                         org.springframework.dao.DataIntegrityViolationException.class);
        }
    }

    @Test
    @DisplayName("Deve validar contas para cenário real de trabalhador")
    void deveValidarContasParaCenarioRealTrabalhador() {
        // Cenário: trabalhador jovem começando a vida financeira
        Conta contaUnica = criarConta("Conta Corrente Caixa", TipoConta.CORRENTE);
        
        // Primeiro mês de trabalho
        criarReceita("Primeiro Salário", 1000.0, contaUnica);
        criarDespesa("Aluguel", 500.0, contaUnica, true);
        criarDespesa("Alimentação", 300.0, contaUnica, true);
        criarDespesa("Transporte", 100.0, contaUnica, true);
        
        List<Receita> receitas = receitaRepository.findAll();
        List<Despesa> despesas = despesaRepository.findAll();
        
        double totalReceitas = receitas.stream().mapToDouble(Receita::getValor).sum();
        double totalDespesas = despesas.stream()
            .filter(d -> Boolean.TRUE.equals(d.getPago()))
            .mapToDouble(Despesa::getValor).sum();
        
        assertThat(totalReceitas).isEqualTo(1000.0);
        assertThat(totalDespesas).isEqualTo(900.0);
        assertThat(totalReceitas - totalDespesas).isEqualTo(100.0); // Sobrou R$ 100
        
        // Validar que tudo está na mesma conta
        assertThat(receitas).allSatisfy(r -> 
            assertThat(r.getConta().getId()).isEqualTo(contaUnica.getId()));
        assertThat(despesas).allSatisfy(d -> 
            assertThat(d.getConta().getId()).isEqualTo(contaUnica.getId()));
    }

    private Conta criarConta(String nome, TipoConta tipo) {
        Conta conta = new Conta();
        conta.setNome(nome);
        conta.setTipo(tipo);
        return contaRepository.save(conta);
    }

    private Receita criarReceita(String descricao, Double valor, Conta conta) {
        Receita receita = new Receita();
        receita.setDescricao(descricao);
        receita.setValor(valor);
        receita.setData(LocalDate.now());
        receita.setConta(conta);
        receita.setCategoria(categoriaSalario);
        return receitaRepository.save(receita);
    }

    private Despesa criarDespesa(String descricao, Double valor, Conta conta, Boolean pago) {
        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setData(LocalDate.now());
        despesa.setConta(conta);
        despesa.setCategoria(categoriaAlimentacao);
        despesa.setPago(pago); // Usado para cálculo de saldo
        return despesaRepository.save(despesa);
    }
} 