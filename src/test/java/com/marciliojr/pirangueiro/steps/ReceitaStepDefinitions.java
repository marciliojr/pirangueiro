package com.marciliojr.pirangueiro.steps;

import com.marciliojr.pirangueiro.dto.ReceitaMensalDTO;
import com.marciliojr.pirangueiro.model.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ReceitaStepDefinitions extends BaseStepDefinitions {
    
    private List<Receita> resultadoBuscaReceitas;
    private Double totalReceitasCalculado;
    private List<ReceitaMensalDTO> dadosGraficoReceitas;
    private Page<Receita> resultadoPaginadoReceitas;
    private Optional<Receita> receitaPorId;
    
    @Dado("ele possui uma conta poupança {string}")
    public void elePossuiUmaContaPoupanca(String nomeConta) {
        Conta conta = criarConta(nomeConta, TipoConta.POUPANCA);
        contas.put(nomeConta, conta);
    }
    
    @Dado("existem as seguintes categorias de receita:")
    public void existemAsSeguintesCategoriasDeReceita(DataTable dataTable) {
        List<Map<String, String>> categoriasList = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> categoriaData : categoriasList) {
            String nome = categoriaData.get("nome");
            String cor = categoriaData.get("cor");
            Categoria categoria = criarCategoria(nome, cor, true);
            categorias.put(nome, categoria);
        }
    }
    
    @Quando("João registra as seguintes receitas no mês atual:")
    public void joaoRegistraAsSeguintesReceitasNoMesAtual(DataTable dataTable) {
        List<Map<String, String>> receitasList = dataTable.asMaps(String.class, String.class);
        quantidadeReceitasEsperada = receitasList.size();
        totalReceitasEsperado = 0.0;
        
        for (Map<String, String> receitaData : receitasList) {
            String descricao = receitaData.get("descricao");
            Double valor = Double.parseDouble(receitaData.get("valor"));
            String categoriaNome = receitaData.get("categoria");
            String contaNome = receitaData.get("conta");
            
            Categoria categoria = categorias.get(categoriaNome);
            Conta conta = contas.get(contaNome);
            
            criarReceita(descricao, valor, LocalDate.now(), conta, categoria);
            totalReceitasEsperado += valor;
        }
    }
    
    @Quando("João registra apenas receitas mínimas:")
    public void joaoRegistraApenasReceitasMinimas(DataTable dataTable) {
        List<Map<String, String>> receitasList = dataTable.asMaps(String.class, String.class);
        quantidadeReceitasEsperada += receitasList.size();
        
        for (Map<String, String> receitaData : receitasList) {
            String descricao = receitaData.get("descricao");
            Double valor = Double.parseDouble(receitaData.get("valor"));
            String categoriaNome = receitaData.get("categoria");
            
            Categoria categoria = categorias.get(categoriaNome);
            Conta conta = contas.values().iterator().next(); // Usa primeira conta disponível
            
            criarReceita(descricao, valor, LocalDate.now(), conta, categoria);
            totalReceitasEsperado += valor;
        }
    }
    
    @Então("devo conseguir buscar todas as receitas com relacionamentos")
    public void devoConseguirBuscarTodasAsReceitasComRelacionamentos() {
        resultadoBuscaReceitas = receitaRepository.findAllWithRelationships();
        assertThat(resultadoBuscaReceitas).isNotNull();
        
        for (Receita receita : resultadoBuscaReceitas) {
            assertThat(receita.getConta()).isNotNull();
            assertThat(receita.getCategoria()).isNotNull();
        }
    }
    
    @Então("devo encontrar {int} receitas cadastradas")
    public void devoEncontrarReceitasCadastradas(Integer quantidade) {
        assertThat(resultadoBuscaReceitas).hasSize(quantidade);
    }
    
    @Então("o total de receitas deve ser R$ {double}")
    public void oTotalDeReceitasDeveSerR$(Double valorEsperado) {
        totalReceitasCalculado = receitaRepository.buscarTotalReceitas();
        assertThat(totalReceitasCalculado).isEqualTo(valorEsperado);
    }
    
    @Então("devo conseguir buscar receitas por categoria {string}")
    public void devoConseguirBuscarReceitasPorCategoria(String nomeCategoria) {
        Categoria categoria = categorias.get(nomeCategoria);
        resultadoBuscaReceitas = receitaRepository.findByCategoria(categoria);
        assertThat(resultadoBuscaReceitas).isNotNull();
    }
    
    @Então("deve retornar {int} receita da categoria {string}")
    public void deveRetornarReceitaDaCategoria(Integer quantidade, String nomeCategoria) {
        assertThat(resultadoBuscaReceitas).hasSize(quantidade);
        for (Receita receita : resultadoBuscaReceitas) {
            assertThat(receita.getCategoria().getNome()).isEqualTo(nomeCategoria);
        }
    }
    
    @Dado("que João tem receitas cadastradas no sistema")
    public void queJoaoTemReceitasCadastradasNoSistema() {
        Categoria categoria = categorias.values().iterator().next();
        Conta conta = contas.values().iterator().next();
        
        criarReceita("Salário mensal", 1000.0, LocalDate.now(), conta, categoria);
        criarReceita("Freelance extra", 250.0, LocalDate.now(), conta, categoria);
    }
    
    @Quando("busco receitas pela descrição {string}")
    public void buscoReceitasPelaDescricao(String descricao) {
        resultadoBuscaReceitas = receitaRepository.findByDescricaoContainingWithRelationships(descricao);
    }
    
    @Então("deve retornar receitas que contenham {string} na descrição")
    public void deveRetornarReceitasQueContenham(String termo) {
        assertThat(resultadoBuscaReceitas).isNotEmpty();
        for (Receita receita : resultadoBuscaReceitas) {
            assertThat(receita.getDescricao().toLowerCase()).contains(termo.toLowerCase());
        }
    }
    
    @Então("as receitas retornadas devem ter seus relacionamentos carregados")
    public void asReceitasRetornadasDevemTerSeusRelacionamentosCarregados() {
        for (Receita receita : resultadoBuscaReceitas) {
            assertThat(receita.getCategoria()).isNotNull();
            assertThat(receita.getConta()).isNotNull();
        }
    }
    
    @Dado("que João tem receitas em diferentes meses")
    public void queJoaoTemReceitasEmDiferentesMeses() {
        Categoria categoria = categorias.values().iterator().next();
        Conta conta = contas.values().iterator().next();
        
        criarReceita("Receita atual", 800.0, LocalDate.now(), conta, categoria);
        criarReceita("Receita passada", 750.0, LocalDate.now().minusMonths(1), conta, categoria);
    }
    
    @Quando("busco receitas do mês atual")
    public void buscoReceitasDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        resultadoBuscaReceitas = receitaRepository.findByMesEAnoWithRelationships(hoje.getMonthValue(), hoje.getYear());
    }
    
    @Então("deve retornar apenas as receitas do mês atual")
    public void deveRetornarApenasAsReceitasDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        assertThat(resultadoBuscaReceitas).isNotEmpty();
        for (Receita receita : resultadoBuscaReceitas) {
            assertThat(receita.getData().getMonthValue()).isEqualTo(hoje.getMonthValue());
            assertThat(receita.getData().getYear()).isEqualTo(hoje.getYear());
        }
    }
    
    @Então("cada receita deve ter conta e categoria carregadas")
    public void cadaReceitaDeveTerContaECategoriaCarregadas() {
        for (Receita receita : resultadoBuscaReceitas) {
            assertThat(receita.getCategoria()).isNotNull();
            assertThat(receita.getConta()).isNotNull();
        }
    }
    
    @Dado("que João tem uma receita específica cadastrada")
    public void queJoaoTemUmaReceitaEspecificaCadastrada() {
        Categoria categoria = categorias.values().iterator().next();
        Conta conta = contas.values().iterator().next();
        
        criarReceita("Receita teste específica", 500.0, LocalDate.now(), conta, categoria);
    }
    
    @Quando("busco a receita por ID com relacionamentos")
    public void buscoAReceitaPorIDComRelacionamentos() {
        List<Receita> todasReceitas = receitaRepository.findAll();
        if (!todasReceitas.isEmpty()) {
            Long id = todasReceitas.get(0).getId();
            receitaPorId = receitaRepository.findByIdWithRelationships(id);
        }
    }
    
    @Então("deve retornar a receita com conta e categoria carregadas")
    public void deveRetornarAReceitaComContaECategoriaCarregadas() {
        assertThat(receitaPorId).isPresent();
        Receita receita = receitaPorId.get();
        assertThat(receita.getConta()).isNotNull();
        assertThat(receita.getCategoria()).isNotNull();
    }
    
    @Então("todos os dados da receita devem estar corretos")
    public void todosOsDadosDaReceitaDevemEstarCorretos() {
        Receita receita = receitaPorId.get();
        assertThat(receita.getDescricao()).isNotNull();
        assertThat(receita.getValor()).isGreaterThan(0.0);
        assertThat(receita.getData()).isNotNull();
    }
    
    @Dado("que João tem múltiplas receitas cadastradas")
    public void queJoaoTemMultiplasReceitasCadastradas() {
        Categoria categoria = categorias.values().iterator().next();
        Conta conta = contas.values().iterator().next();
        
        criarReceita("Freelance desenvolvimento", 400.0, LocalDate.of(2024, 12, 15), conta, categoria);
        criarReceita("Consulta freelance", 200.0, LocalDate.of(2024, 12, 20), conta, categoria);
        criarReceita("Projeto freelance", 600.0, LocalDate.of(2024, 12, 25), conta, categoria);
    }
    
    @Quando("busco receitas com filtros usando paginação:")
    public void buscoReceitasComFiltrosUsandoPaginacao(DataTable dataTable) {
        Map<String, String> filtros = dataTable.asMap(String.class, String.class);
        
        String descricao = filtros.get("descrição");
        Integer mes = Integer.parseInt(filtros.get("mês"));
        Integer ano = Integer.parseInt(filtros.get("ano"));
        
        PageRequest pageRequest = PageRequest.of(0, 10);
        resultadoPaginadoReceitas = receitaRepository.findByFiltros(descricao, mes, ano, pageRequest);
    }
    
    @Então("deve retornar as receitas que atendem aos filtros")
    public void deveRetornarAsReceitasQueAtendemAosFiltros() {
        assertThat(resultadoPaginadoReceitas).isNotNull();
        assertThat(resultadoPaginadoReceitas.getContent()).isNotEmpty();
    }
    
    @Então("o resultado deve estar paginado corretamente")
    public void oResultadoDeveEstarPaginadoCorretamente() {
        assertThat(resultadoPaginadoReceitas.getNumber()).isEqualTo(0); // primeira página
        assertThat(resultadoPaginadoReceitas.getSize()).isEqualTo(10); // tamanho da página
    }
    
    @Então("as receitas devem ter relacionamentos carregados")
    public void asReceitasDevemTerRelacionamentosCarregados() {
        for (Receita receita : resultadoPaginadoReceitas.getContent()) {
            assertThat(receita.getCategoria()).isNotNull();
            assertThat(receita.getConta()).isNotNull();
        }
    }
    
    @Dado("que João quer acompanhar sua renda mensal")
    public void queJoaoQuerAcompanharSuaRendaMensal() {
        // Setup já feito no contexto
    }
    
    @Quando("calculo o total de receitas do mês atual")
    public void calculoOTotalDeReceitasDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        totalReceitasCalculado = receitaRepository.buscarTotalReceitasPorMesAno(hoje.getMonthValue(), hoje.getYear());
    }
    
    @Então("deve retornar a soma correta de todas as receitas do mês")
    public void deveRetornarASomaCorretaDeTodasAsReceitasDoMes() {
        assertThat(totalReceitasCalculado).isNotNull();
        assertThat(totalReceitasCalculado).isGreaterThanOrEqualTo(0.0);
    }
    
    @Então("o valor deve ser maior que zero se houver receitas")
    public void oValorDeveSerMaiorQueZeroSeHouverReceitas() {
        if (quantidadeReceitasEsperada > 0) {
            assertThat(totalReceitasCalculado).isGreaterThan(0.0);
        }
    }
    
    @Dado("que João quer visualizar sua renda em um gráfico")
    public void queJoaoQuerVisualizarSuaRendaEmUmGrafico() {
        // Setup já feito no contexto
    }
    
    @Quando("busco dados de receitas agrupados por mês no último trimestre")
    public void buscoDadosDeReceitasAgrupadosPorMesNoUltimoTrimestre() {
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusMonths(3);
        
        dadosGraficoReceitas = receitaRepository.buscarReceitasAgrupadasPorMes(dataInicio, dataFim);
    }
    
    @Então("deve retornar os totais mensais de receitas corretamente")
    public void deveRetornarOsTotaisMensaisDeReceitasCorretamente() {
        assertThat(dadosGraficoReceitas).isNotNull();
        if (quantidadeReceitasEsperada > 0) {
            assertThat(dadosGraficoReceitas).isNotEmpty();
        }
    }
    
    @Então("os dados devem estar no formato DTO apropriado para gráficos")
    public void osDadosDevemEstarNoFormatoDTOApropriadoParaGraficos() {
        for (ReceitaMensalDTO dto : dadosGraficoReceitas) {
            assertThat(dto.getMes()).isNotNull();
            assertThat(dto.getTotal()).isNotNull();
            assertThat(dto.getTotal()).isGreaterThanOrEqualTo(0.0);
        }
    }
    
    @Então("devo conseguir cadastrar todas as receitas")
    public void devoConseguirCadastrarTodasAsReceitas() {
        List<Receita> todasReceitas = receitaRepository.findAll();
        assertThat(todasReceitas).hasSize(quantidadeReceitasEsperada);
    }
    
    @Então("deve ser possível buscar essas receitas por filtros")
    public void deveSerPossiveLBuscarEssasReceitasPorFiltros() {
        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Receita> receitasFiltradas = receitaRepository.findByFiltros(null, null, null, pageRequest);
        assertThat(receitasFiltradas.getContent()).hasSizeGreaterThanOrEqualTo(quantidadeReceitasEsperada);
    }
    
    @Então("as consultas devem retornar valores corretos mesmo com renda baixa")
    public void asConsultasDevemRetornarValoresCorretosMessoComRendaBaixa() {
        Double totalGeral = receitaRepository.buscarTotalReceitas();
        assertThat(totalGeral).isEqualTo(totalReceitasEsperado);
        assertThat(totalGeral).isGreaterThan(0.0);
    }
    
    @Dado("que não existem receitas cadastradas para um mês específico")
    public void queNaoExistemReceitasCadastradasParaUmMesEspecifico() {
        // Não precisa fazer nada, não há receitas cadastradas
    }
    
    @Quando("busco receitas desse mês")
    public void buscoReceitasDesteMes() {
        LocalDate mesAnterior = LocalDate.now().minusMonths(2);
        resultadoBuscaReceitas = receitaRepository.findByMesEAno(mesAnterior.getMonthValue(), mesAnterior.getYear());
    }
    
    @Então("deve retornar uma lista vazia")
    public void deveRetornarUmaListaVazia() {
        assertThat(resultadoBuscaReceitas).isEmpty();
    }
    
    @Então("o total de receitas do mês deve ser R$ {double}")
    public void oTotalDeReceitasDoMesDeveSerR$(Double valorEsperado) {
        LocalDate mesAnterior = LocalDate.now().minusMonths(2);
        Double total = receitaRepository.buscarTotalReceitasPorMesAno(mesAnterior.getMonthValue(), mesAnterior.getYear());
        assertThat(total).isEqualTo(valorEsperado);
    }
    
    @Então("as consultas para gráficos devem tratar corretamente a ausência de dados")
    public void asConsultasParaGraficosDevemTratarCorretamenteAAusenciaDeDados() {
        LocalDate dataFim = LocalDate.now().minusMonths(2);
        LocalDate dataInicio = dataFim.minusMonths(1);
        
        List<ReceitaMensalDTO> dados = receitaRepository.buscarReceitasAgrupadasPorMes(dataInicio, dataFim);
        assertThat(dados).isNotNull();
        // Pode ser vazia, mas não deve dar erro
    }
    
    @Dado("que João tem receitas de múltiplas categorias")
    public void queJoaoTemReceitasDeMultiplasCategorias() {
        Conta conta = contas.values().iterator().next();
        
        for (Categoria categoria : categorias.values()) {
            criarReceita("Receita " + categoria.getNome(), 100.0, LocalDate.now(), conta, categoria);
        }
    }
    
    @Quando("busco receitas por categoria específica")
    public void buscoReceitasPorCategoriaEspecifica() {
        Categoria categoria = categorias.values().iterator().next();
        resultadoBuscaReceitas = receitaRepository.findByCategoria(categoria);
    }
    
    @Então("deve retornar apenas receitas da categoria solicitada")
    public void deveRetornarApenasReceitasDaCategoriasolicitada() {
        assertThat(resultadoBuscaReceitas).isNotEmpty();
        Categoria categoriaEsperada = categorias.values().iterator().next();
        
        for (Receita receita : resultadoBuscaReceitas) {
            assertThat(receita.getCategoria().getNome()).isEqualTo(categoriaEsperada.getNome());
        }
    }
    
    @Então("cada receita deve manter seus relacionamentos")
    public void cadaReceitaDeveMantSeusRelacionamentos() {
        for (Receita receita : resultadoBuscaReceitas) {
            assertThat(receita.getCategoria()).isNotNull();
            assertThat(receita.getConta()).isNotNull();
        }
    }
    
    @Então("a busca deve ser insensível a maiúsculas e minúsculas")
    public void aBuscaDeveSerInsensiveeLMaiusculasEMinusculas() {
        // Testa busca por descrição insensível a caso
        List<Receita> resultadoMaiuscula = receitaRepository.findByDescricaoContainingIgnoreCase("RECEITA");
        List<Receita> resultadoMinuscula = receitaRepository.findByDescricaoContainingIgnoreCase("receita");
        
        assertThat(resultadoMaiuscula).hasSameSizeAs(resultadoMinuscula);
    }
} 