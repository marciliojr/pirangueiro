package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Despesa;
import com.marciliojr.pirangueiro.model.Conta;
import com.marciliojr.pirangueiro.model.Cartao;
import com.marciliojr.pirangueiro.model.Categoria;
import com.marciliojr.pirangueiro.model.TipoConta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DespesaRepositoryTest {

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Conta conta;
    private Cartao cartao;
    private Categoria categoria;
    private Despesa despesa;

    @BeforeEach
    void setUp() {
        // Configurando conta
        conta = new Conta();
        conta.setNome("Conta Teste");
        conta.setTipo(TipoConta.CORRENTE);
        conta = contaRepository.save(conta);

        // Configurando cartão
        cartao = new Cartao();
        cartao.setNome("Cartão Teste");
        cartao.setLimite(1000.0);
        cartao.setLimiteUsado(0.0);
        cartao.setDiaFechamento(10);
        cartao.setDiaVencimento(15);
        cartao = cartaoRepository.save(cartao);

        // Configurando categoria
        categoria = new Categoria();
        categoria.setNome("Categoria Teste");
        categoria = categoriaRepository.save(categoria);

        // Configurando despesa
        despesa = new Despesa();
        despesa.setDescricao("Despesa Teste");
        despesa.setValor(100.0);
        despesa.setData(LocalDate.now());
        despesa.setConta(conta);
        despesa.setCartao(cartao);
        despesa.setCategoria(categoria);
        despesa.setObservacao("Observação teste");
        despesa = despesaRepository.save(despesa);
    }

    @Test
    void deveEncontrarDespesaPorId() {
        Optional<Despesa> despesaEncontrada = despesaRepository.findById(despesa.getId());
        
        assertThat(despesaEncontrada)
            .isPresent()
            .get()
            .satisfies(d -> {
                assertThat(d.getDescricao()).isEqualTo("Despesa Teste");
                assertThat(d.getValor()).isEqualTo(100.0);
            });
    }

    @Test
    void deveEncontrarDespesaPorDescricao() {
        List<Despesa> despesas = despesaRepository.findByDescricaoContainingIgnoreCase("teste");
        
        assertThat(despesas)
            .isNotEmpty()
            .hasSize(1)
            .first()
            .satisfies(d -> {
                assertThat(d.getDescricao()).isEqualTo("Despesa Teste");
                assertThat(d.getValor()).isEqualTo(100.0);
            });
    }

    @Test
    void deveEncontrarDespesaPorMesEAno() {
        LocalDate hoje = LocalDate.now();
        List<Despesa> despesas = despesaRepository.findByMesEAno(hoje.getMonthValue(), hoje.getYear());
        
        assertThat(despesas)
            .isNotEmpty()
            .hasSize(1)
            .first()
            .satisfies(d -> {
                assertThat(d.getDescricao()).isEqualTo("Despesa Teste");
                assertThat(d.getValor()).isEqualTo(100.0);
            });
    }

    @Test
    void deveEncontrarDespesaPorCategoria() {
        List<Despesa> despesas = despesaRepository.findByCategoria(categoria);
        
        assertThat(despesas)
            .isNotEmpty()
            .hasSize(1)
            .first()
            .satisfies(d -> {
                assertThat(d.getDescricao()).isEqualTo("Despesa Teste");
                assertThat(d.getValor()).isEqualTo(100.0);
                assertThat(d.getCategoria().getNome()).isEqualTo("Categoria Teste");
            });
    }

    @Test
    void deveBuscarDespesaPorDescricaoContaCartaoData() {
        LocalDate hoje = LocalDate.now();
        List<Despesa> despesas = despesaRepository.buscarPorDescricaoContaCartaoData(
            "Teste",
            conta.getId(),
            cartao.getId(),
            hoje
        );
        
        assertThat(despesas)
            .isNotEmpty()
            .hasSize(1)
            .first()
            .satisfies(d -> {
                assertThat(d.getDescricao()).isEqualTo("Despesa Teste");
                assertThat(d.getValor()).isEqualTo(100.0);
                assertThat(d.getConta().getId()).isEqualTo(conta.getId());
                assertThat(d.getCartao().getId()).isEqualTo(cartao.getId());
                assertThat(d.getData()).isEqualTo(hoje);
            });
    }

    @Test
    void deveEncontrarTodasDespesasComRelacionamentos() {
        List<Despesa> despesas = despesaRepository.findAllWithRelationships();
        
        assertThat(despesas)
            .isNotEmpty()
            .hasSize(1)
            .first()
            .satisfies(d -> {
                assertThat(d.getDescricao()).isEqualTo("Despesa Teste");
                assertThat(d.getValor()).isEqualTo(100.0);
                assertThat(d.getConta()).isNotNull();
                assertThat(d.getCartao()).isNotNull();
                assertThat(d.getCategoria()).isNotNull();
            });
    }

    @Test
    void deveEncontrarDespesaPorIdComRelacionamentos() {
        Optional<Despesa> despesaEncontrada = despesaRepository.findByIdWithRelationships(despesa.getId());
        
        assertThat(despesaEncontrada)
            .isPresent()
            .get()
            .satisfies(d -> {
                assertThat(d.getDescricao()).isEqualTo("Despesa Teste");
                assertThat(d.getValor()).isEqualTo(100.0);
                assertThat(d.getConta()).isNotNull();
                assertThat(d.getCartao()).isNotNull();
                assertThat(d.getCategoria()).isNotNull();
            });
    }

    @Test
    void deveBuscarDespesaApenasComData() {
        LocalDate hoje = LocalDate.now();
        List<Despesa> despesas = despesaRepository.buscarPorDescricaoContaCartaoData(
            null,   // descrição nula
            null,   // conta nula
            null,   // cartão nulo
            hoje    // apenas data
        );
        
        assertThat(despesas)
            .isNotEmpty()
            .hasSize(1)
            .first()
            .satisfies(d -> {
                assertThat(d.getData()).isEqualTo(hoje);
                assertThat(d.getDescricao()).isEqualTo("Despesa Teste");
                assertThat(d.getValor()).isEqualTo(100.0);
            });
    }
} 