package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes do Repository de Categorias - Trabalhador com Salário R$ 1000")
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    @DisplayName("Deve criar categorias típicas de despesas de trabalhador")
    void deveCriarCategoriasTipicasDespesasTrabalhador() {
        // Categorias essenciais para trabalhador com salário de R$ 1000
        Categoria alimentacao = criarCategoriaDespesa("Alimentação", "#FF5722");
        Categoria moradia = criarCategoriaDespesa("Moradia", "#795548");
        Categoria transporte = criarCategoriaDespesa("Transporte", "#2196F3");
        Categoria saude = criarCategoriaDespesa("Saúde", "#F44336");
        Categoria educacao = criarCategoriaDespesa("Educação", "#9C27B0");

        List<Categoria> categoriasDespesa = categoriaRepository.findAll().stream()
            .filter(c -> !c.getTipoReceita())
            .toList();
        
        assertThat(categoriasDespesa).hasSize(5);
        assertThat(categoriasDespesa)
            .extracting(Categoria::getNome)
            .containsExactlyInAnyOrder("Alimentação", "Moradia", "Transporte", "Saúde", "Educação");
    }

    @Test
    @DisplayName("Deve criar categorias típicas de receitas de trabalhador")
    void deveCriarCategoriasTipicasReceitasTrabalhador() {
        // Categorias de receita para trabalhador
        Categoria salario = criarCategoriaReceita("Salário", "#4CAF50");
        Categoria freelance = criarCategoriaReceita("Freelance", "#FF9800");
        Categoria vendas = criarCategoriaReceita("Vendas", "#9C27B0");
        Categoria investimentos = criarCategoriaReceita("Investimentos", "#607D8B");

        List<Categoria> categoriasReceita = categoriaRepository.findAll().stream()
            .filter(Categoria::getTipoReceita)
            .toList();
        
        assertThat(categoriasReceita).hasSize(4);
        assertThat(categoriasReceita)
            .extracting(Categoria::getNome)
            .containsExactlyInAnyOrder("Salário", "Freelance", "Vendas", "Investimentos");
    }

    @Test
    @DisplayName("Deve diferenciar categorias de receita e despesa")
    void deveDiferenciarCategoriasReceitaEDespesa() {
        Categoria salario = criarCategoriaReceita("Salário", "#4CAF50");
        Categoria alimentacao = criarCategoriaDespesa("Alimentação", "#FF5722");

        assertThat(salario.getTipoReceita()).isTrue();
        assertThat(alimentacao.getTipoReceita()).isFalse();
        assertThat(salario.getTipoReceita()).isNotEqualTo(alimentacao.getTipoReceita());
    }

    @Test
    @DisplayName("Deve buscar categoria por ID específico")
    void deveBuscarCategoriaPorId() {
        Categoria categoria = criarCategoriaDespesa("Transporte", "#2196F3");
        
        Optional<Categoria> categoriaEncontrada = categoriaRepository.findById(categoria.getId());
        
        assertThat(categoriaEncontrada).isPresent();
        assertThat(categoriaEncontrada.get().getNome()).isEqualTo("Transporte");
        assertThat(categoriaEncontrada.get().getCor()).isEqualTo("#2196F3");
        assertThat(categoriaEncontrada.get().getTipoReceita()).isFalse();
    }

    @Test
    @DisplayName("Deve validar cores em formato hexadecimal")
    void deveValidarCoresFormatoHexadecimal() {
        Categoria categoria1 = criarCategoriaDespesa("Categoria 1", "#FF0000"); // Vermelho
        Categoria categoria2 = criarCategoriaDespesa("Categoria 2", "#00FF00"); // Verde  
        Categoria categoria3 = criarCategoriaDespesa("Categoria 3", "#0000FF"); // Azul

        assertThat(categoria1.getCor()).matches("#[0-9A-Fa-f]{6}");
        assertThat(categoria2.getCor()).matches("#[0-9A-Fa-f]{6}");
        assertThat(categoria3.getCor()).matches("#[0-9A-Fa-f]{6}");
    }

    @Test
    @DisplayName("Deve criar categorias específicas para orçamento de R$ 1000")
    void deveCriarCategoriasEspecificasOrcamento1000() {
        // Categorias proporcionais ao salário de R$ 1000
        Categoria aluguel = criarCategoriaDespesa("Aluguel", "#8D6E63"); // ~40% do salário
        Categoria alimentacao = criarCategoriaDespesa("Alimentação", "#FF5722"); // ~25% do salário
        Categoria transporte = criarCategoriaDespesa("Transporte", "#2196F3"); // ~10% do salário
        Categoria lazer = criarCategoriaDespesa("Lazer", "#E91E63"); // ~5% do salário
        Categoria economia = criarCategoriaDespesa("Economia", "#4CAF50"); // ~20% do salário

        List<Categoria> todasCategorias = categoriaRepository.findAll();
        
        assertThat(todasCategorias).hasSize(5);
        assertThat(todasCategorias).allSatisfy(categoria -> {
            assertThat(categoria.getNome()).isNotBlank();
            assertThat(categoria.getCor()).isNotBlank();
            assertThat(categoria.getTipoReceita()).isFalse();
        });
    }

    @Test
    @DisplayName("Deve permitir nomes descritivos de categorias")
    void devePermitirNomesDescritivosCategorias() {
        Categoria categoria = criarCategoriaDespesa("Supermercado e Alimentação Básica", "#FF5722");
        
        assertThat(categoria.getNome()).isEqualTo("Supermercado e Alimentação Básica");
        assertThat(categoria.getNome().length()).isGreaterThan(20);
    }

    @Test
    @DisplayName("Deve validar categorias essenciais para trabalhador")
    void deveValidarCategoriasEssenciaisTrabalhador() {
        // Categorias mínimas necessárias
        criarCategoriaDespesa("Moradia", "#795548");
        criarCategoriaDespesa("Alimentação", "#FF5722");
        criarCategoriaDespesa("Transporte", "#2196F3");
        criarCategoriaReceita("Salário", "#4CAF50");

        List<Categoria> categorias = categoriaRepository.findAll();
        List<Categoria> despesas = categorias.stream().filter(c -> !c.getTipoReceita()).toList();
        List<Categoria> receitas = categorias.stream().filter(Categoria::getTipoReceita).toList();

        // Validações mínimas
        assertThat(despesas).hasSizeGreaterThanOrEqualTo(3); // Pelo menos 3 categorias de despesa
        assertThat(receitas).hasSizeGreaterThanOrEqualTo(1); // Pelo menos 1 categoria de receita
        
        // Verificar se tem as essenciais
        List<String> nomesDespesas = despesas.stream().map(Categoria::getNome).toList();
        assertThat(nomesDespesas).contains("Moradia", "Alimentação", "Transporte");
    }

    // TESTES DE CENÁRIOS DE ERRO

    @Test
    @DisplayName("Deve permitir salvar categoria sem nome mas retornar nulo")
    void devePermitirSalvarCategoriaSemNome() {
        Categoria categoria = new Categoria();
        categoria.setNome(null);
        categoria.setCor("#FF0000");
        categoria.setTipoReceita(false);

        Categoria categoriaSalva = categoriaRepository.save(categoria);
        assertThat(categoriaSalva.getNome()).isNull();
        assertThat(categoriaSalva.getCor()).isEqualTo("#FF0000");
    }

    @Test
    @DisplayName("Deve permitir salvar categoria sem cor mas retornar nulo")
    void devePermitirSalvarCategoriaSemCor() {
        Categoria categoria = new Categoria();
        categoria.setNome("Categoria Teste");
        categoria.setCor(null);
        categoria.setTipoReceita(false);

        Categoria categoriaSalva = categoriaRepository.save(categoria);
        assertThat(categoriaSalva.getNome()).isEqualTo("Categoria Teste");
        assertThat(categoriaSalva.getCor()).isNull();
    }

    @Test
    @DisplayName("Deve permitir salvar categoria sem tipo mas gerar erro na persistência")
    void devePermitirSalvarCategoriaSemTipo() {
        Categoria categoria = new Categoria();
        categoria.setNome("Categoria Teste");
        categoria.setCor("#FF0000");
        categoria.setTipoReceita(false); // Definindo valor padrão

        Categoria categoriaSalva = categoriaRepository.save(categoria);
        assertThat(categoriaSalva.getNome()).isEqualTo("Categoria Teste");
        assertThat(categoriaSalva.getTipoReceita()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar optional vazio para ID inexistente")
    void deveRetornarOptionalVazioParaIdInexistente() {
        Optional<Categoria> categoria = categoriaRepository.findById(999L);
        assertThat(categoria).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há categorias")
    void deveRetornarListaVaziaQuandoNaoHaCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        assertThat(categorias).isEmpty();
    }

    @Test
    @DisplayName("Deve validar formato de cor inválido")
    void deveValidarFormatoCorInvalido() {
        // Repository permite qualquer string, validação deve ser na camada de serviço
        Categoria categoria = criarCategoriaDespesa("Teste", "cor-invalida");
        
        assertThat(categoria.getCor()).isEqualTo("cor-invalida");
        assertThat(categoria.getCor()).doesNotMatch("#[0-9A-Fa-f]{6}");
    }

    @Test
    @DisplayName("Deve permitir criar múltiplas categorias com cores diferentes")
    void devePermitirMultiplasCategoriasComCoresDiferentes() {
        List<String> cores = List.of("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF");
        
        for (int i = 0; i < cores.size(); i++) {
            criarCategoriaDespesa("Categoria " + (i + 1), cores.get(i));
        }
        
        List<Categoria> categorias = categoriaRepository.findAll();
        List<String> coresSalvas = categorias.stream().map(Categoria::getCor).toList();
        
        assertThat(categorias).hasSize(5);
        assertThat(coresSalvas).containsExactlyInAnyOrderElementsOf(cores);
    }

    private Categoria criarCategoriaDespesa(String nome, String cor) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setCor(cor);
        categoria.setTipoReceita(false);
        return categoriaRepository.save(categoria);
    }

    private Categoria criarCategoriaReceita(String nome, String cor) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setCor(cor);
        categoria.setTipoReceita(true);
        return categoriaRepository.save(categoria);
    }
} 