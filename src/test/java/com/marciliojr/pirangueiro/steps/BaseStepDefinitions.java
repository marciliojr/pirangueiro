package com.marciliojr.pirangueiro.steps;

import com.marciliojr.pirangueiro.model.*;
import com.marciliojr.pirangueiro.repository.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@CucumberContextConfiguration
@DataJpaTest
@ActiveProfiles("test")
public class BaseStepDefinitions {
    
    @Autowired
    protected DespesaRepository despesaRepository;
    
    @Autowired
    protected ReceitaRepository receitaRepository;
    
    @Autowired
    protected ContaRepository contaRepository;
    
    @Autowired
    protected CartaoRepository cartaoRepository;
    
    @Autowired
    protected CategoriaRepository categoriaRepository;
    
    // Contexto compartilhado entre os steps
    protected Map<String, Conta> contas = new HashMap<>();
    protected Map<String, Cartao> cartoes = new HashMap<>();
    protected Map<String, Categoria> categorias = new HashMap<>();
    protected String nomeUsuario;
    
    // Dados de controle para validações
    protected Double totalDespesasEsperado = 0.0;
    protected Double totalReceitasEsperado = 0.0;
    protected Integer quantidadeDespesasEsperada = 0;
    protected Integer quantidadeReceitasEsperada = 0;
    
    protected void limparContexto() {
        contas.clear();
        cartoes.clear();
        categorias.clear();
        nomeUsuario = null;
        totalDespesasEsperado = 0.0;
        totalReceitasEsperado = 0.0;
        quantidadeDespesasEsperada = 0;
        quantidadeReceitasEsperada = 0;
    }
    
    protected Conta criarConta(String nome, TipoConta tipo) {
        Conta conta = new Conta();
        conta.setNome(nome);
        conta.setTipo(tipo);
        return contaRepository.save(conta);
    }
    
    protected Cartao criarCartao(String nome, Double limite) {
        Cartao cartao = new Cartao();
        cartao.setNome(nome);
        cartao.setLimite(limite);
        cartao.setLimiteUsado(0.0);
        cartao.setDiaFechamento(10);
        cartao.setDiaVencimento(15);
        return cartaoRepository.save(cartao);
    }
    
    protected Categoria criarCategoria(String nome, String cor, Boolean tipoReceita) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setCor(cor);
        categoria.setTipoReceita(tipoReceita);
        return categoriaRepository.save(categoria);
    }
    
    protected Despesa criarDespesa(String descricao, Double valor, LocalDate data, 
                                 Conta conta, Cartao cartao, Categoria categoria) {
        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setData(data);
        despesa.setConta(conta);
        despesa.setCartao(cartao);
        despesa.setCategoria(categoria);
        // Ignora campo pago conforme solicitado
        return despesaRepository.save(despesa);
    }
    
    protected Receita criarReceita(String descricao, Double valor, LocalDate data, 
                                 Conta conta, Categoria categoria) {
        Receita receita = new Receita();
        receita.setDescricao(descricao);
        receita.setValor(valor);
        receita.setData(data);
        receita.setConta(conta);
        receita.setCategoria(categoria);
        return receitaRepository.save(receita);
    }
} 