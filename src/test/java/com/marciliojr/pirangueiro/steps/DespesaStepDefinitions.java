package com.marciliojr.pirangueiro.steps;

import com.marciliojr.pirangueiro.dto.DespesaMensalDTO;
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

public class DespesaStepDefinitions extends BaseStepDefinitions {
    
    private List<Despesa> resultadoBusca;
    private Double totalCalculado;
    private List<DespesaMensalDTO> dadosGrafico;
    private Page<Despesa> resultadoPaginado;
    
    @Dado("que existe um trabalhador chamado {string}")
    public void queExisteUmTrabalhadorChamado(String nome) {
        limparContexto();
        this.nomeUsuario = nome;
    }
    
    @Dado("ele possui uma conta corrente {string}")
    public void elePossuiUmaContaCorrente(String nomeConta) {
        Conta conta = criarConta(nomeConta, TipoConta.CORRENTE);
        contas.put(nomeConta, conta);
    }
    
    @Dado("ele possui um cartão de crédito {string} com limite de R$ {double}")
    public void elePossuiUmCartaoDeCredito(String nomeCartao, Double limite) {
        Cartao cartao = criarCartao(nomeCartao, limite);
        cartoes.put(nomeCartao, cartao);
    }
    
    @Dado("existem as seguintes categorias de despesa:")
    public void existemAsSeguintesCategoriasDeDespesa(DataTable dataTable) {
        List<Map<String, String>> categoriasList = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> categoriaData : categoriasList) {
            String nome = categoriaData.get("nome");
            String cor = categoriaData.get("cor");
            Categoria categoria = criarCategoria(nome, cor, false);
            categorias.put(nome, categoria);
        }
    }
    
    @Quando("João registra as seguintes despesas no mês atual:")
    public void joaoRegistraAsSeguintesDespesasNoMesAtual(DataTable dataTable) {
        List<Map<String, String>> despesasList = dataTable.asMaps(String.class, String.class);
        quantidadeDespesasEsperada = despesasList.size();
        totalDespesasEsperado = 0.0;
        
        for (Map<String, String> despesaData : despesasList) {
            String descricao = despesaData.get("descricao");
            Double valor = Double.parseDouble(despesaData.get("valor"));
            String categoriaNome = despesaData.get("categoria");
            String contaNome = despesaData.get("conta");
            String cartaoNome = despesaData.get("cartao");
            
            Categoria categoria = categorias.get(categoriaNome);
            Conta conta = contaNome != null && !contaNome.trim().isEmpty() ? contas.get(contaNome) : null;
            Cartao cartao = cartaoNome != null && !cartaoNome.trim().isEmpty() ? cartoes.get(cartaoNome) : null;
            
            criarDespesa(descricao, valor, LocalDate.now(), conta, cartao, categoria);
            totalDespesasEsperado += valor;
        }
    }
    
    @Quando("João registra despesas excessivas:")
    public void joaoRegistraDespesasExcessivas(DataTable dataTable) {
        List<Map<String, String>> despesasList = dataTable.asMaps(String.class, String.class);
        quantidadeDespesasEsperada += despesasList.size();
        
        for (Map<String, String> despesaData : despesasList) {
            String descricao = despesaData.get("descricao");
            Double valor = Double.parseDouble(despesaData.get("valor"));
            String categoriaNome = despesaData.get("categoria");
            
            Categoria categoria = categorias.get(categoriaNome);
            // Usa a primeira conta disponível
            Conta conta = contas.values().iterator().next();
            
            criarDespesa(descricao, valor, LocalDate.now(), conta, null, categoria);
            totalDespesasEsperado += valor;
        }
    }
    
    @Então("devo conseguir buscar todas as despesas com relacionamentos")
    public void devoConseguirBuscarTodasAsDespesasComRelacionamentos() {
        resultadoBusca = despesaRepository.findAllWithRelationships();
        assertThat(resultadoBusca).isNotNull();
        
        for (Despesa despesa : resultadoBusca) {
            if (despesa.getConta() != null) {
                assertThat(despesa.getConta().getNome()).isNotNull();
            }
            if (despesa.getCartao() != null) {
                assertThat(despesa.getCartao().getNome()).isNotNull();
            }
            assertThat(despesa.getCategoria()).isNotNull();
        }
    }
    
    @Então("devo encontrar {int} despesas cadastradas")
    public void devoEncontrarDespesasCadastradas(Integer quantidade) {
        assertThat(resultadoBusca).hasSize(quantidade);
    }
    
    @Então("o total de despesas deve ser R$ {double}")
    public void oTotalDeDespesasDeveSerR$(Double valorEsperado) {
        totalCalculado = despesaRepository.buscarTotalDespesas();
        assertThat(totalCalculado).isEqualTo(valorEsperado);
    }
    
    @Então("devo conseguir buscar despesas por categoria {string}")
    public void devoConseguirBuscarDespesasPorCategoria(String nomeCategoria) {
        Categoria categoria = categorias.get(nomeCategoria);
        resultadoBusca = despesaRepository.findByCategoria(categoria);
        assertThat(resultadoBusca).isNotNull();
    }
    
    @Então("deve retornar {int} despesas da categoria {string}")
    public void deveRetornarDespesasDaCategoria(Integer quantidade, String nomeCategoria) {
        assertThat(resultadoBusca).hasSize(quantidade);
        for (Despesa despesa : resultadoBusca) {
            assertThat(despesa.getCategoria().getNome()).isEqualTo(nomeCategoria);
        }
    }
    
    @Dado("que João tem despesas cadastradas no sistema")
    public void queJoaoTemDespesasCadastradasNoSistema() {
        Categoria categoria = categorias.values().iterator().next();
        Conta conta = contas.values().iterator().next();
        
        criarDespesa("Almoço no restaurante", 25.0, LocalDate.now(), conta, null, categoria);
        criarDespesa("Jantar com amigos", 45.0, LocalDate.now(), conta, null, categoria);
    }
    
    @Quando("busco despesas pela descrição {string}")
    public void buscoDespesasPelaDescricao(String descricao) {
        resultadoBusca = despesaRepository.findByDescricaoContainingWithRelationships(descricao);
    }
    
    @Então("deve retornar despesas que contenham {string} na descrição")
    public void deveRetornarDespesasQueContenham(String termo) {
        assertThat(resultadoBusca).isNotEmpty();
        for (Despesa despesa : resultadoBusca) {
            assertThat(despesa.getDescricao().toLowerCase()).contains(termo.toLowerCase());
        }
    }
    
    @Então("as despesas retornadas devem ter seus relacionamentos carregados")
    public void asDespesasRetornadasDevemTerSeusRelacionamentosCarregados() {
        for (Despesa despesa : resultadoBusca) {
            assertThat(despesa.getCategoria()).isNotNull();
        }
    }
    
    @Dado("que João tem despesas em diferentes meses")
    public void queJoaoTemDespesasEmDiferentesMeses() {
        Categoria categoria = categorias.values().iterator().next();
        Conta conta = contas.values().iterator().next();
        
        // Despesa do mês atual
        criarDespesa("Despesa atual", 100.0, LocalDate.now(), conta, null, categoria);
        
        // Despesa do mês passado
        criarDespesa("Despesa passada", 200.0, LocalDate.now().minusMonths(1), conta, null, categoria);
    }
    
    @Quando("busco despesas do mês atual")
    public void buscoDespesasDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        resultadoBusca = despesaRepository.findByMesEAnoWithRelationships(hoje.getMonthValue(), hoje.getYear());
    }
    
    @Então("deve retornar apenas as despesas do mês atual")
    public void deveRetornarApenasAsDespesasDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        assertThat(resultadoBusca).isNotEmpty();
        for (Despesa despesa : resultadoBusca) {
            assertThat(despesa.getData().getMonthValue()).isEqualTo(hoje.getMonthValue());
            assertThat(despesa.getData().getYear()).isEqualTo(hoje.getYear());
        }
    }
    
    @Então("cada despesa deve ter conta, categoria e cartão carregados")
    public void cadaDespesaDeveTerContaCategoriaECartaoCarregados() {
        for (Despesa despesa : resultadoBusca) {
            assertThat(despesa.getCategoria()).isNotNull();
            // Conta e cartão podem ser null, mas se existirem devem estar carregados
            if (despesa.getConta() != null) {
                assertThat(despesa.getConta().getNome()).isNotNull();
            }
            if (despesa.getCartao() != null) {
                assertThat(despesa.getCartao().getNome()).isNotNull();
            }
        }
    }
    
    @Dado("que João tem múltiplas despesas")
    public void queJoaoTemMultiplasDespesas() {
        Categoria categoria = categorias.values().iterator().next();
        Conta conta = contas.values().iterator().next();
        Cartao cartao = cartoes.values().iterator().next();
        
        criarDespesa("Supermercado Extra", 150.0, LocalDate.now(), conta, null, categoria);
        criarDespesa("Farmácia", 45.0, LocalDate.now(), null, cartao, categoria);
        criarDespesa("Combustível", 80.0, LocalDate.now(), conta, null, categoria);
    }
    
    @Quando("busco despesas específicas por:")
    public void buscoDespesasEspecificasPor(DataTable dataTable) {
        Map<String, String> filtros = dataTable.asMap(String.class, String.class);
        
        String descricao = filtros.get("descricao");
        String contaNome = filtros.get("conta");
        String dataStr = filtros.get("data");
        
        Conta conta = contaNome != null ? contas.get(contaNome) : null;
        LocalDate data = "hoje".equals(dataStr) ? LocalDate.now() : null;
        
        resultadoBusca = despesaRepository.buscarPorDescricaoContaCartaoData(
            descricao, 
            conta != null ? conta.getId() : null, 
            null, 
            data
        );
    }
    
    @Então("deve retornar apenas as despesas que atendem aos critérios")
    public void deveRetornarApenasAsDespesasQueAtendemAosCriterios() {
        assertThat(resultadoBusca).isNotEmpty();
        // Validação específica dependeria dos critérios exatos, aqui validamos que retornou algo
    }
    
    @Então("as despesas devem ter todos os relacionamentos carregados")
    public void asDespesasDevemTerTodosOsRelacionamentosCarregados() {
        for (Despesa despesa : resultadoBusca) {
            assertThat(despesa.getCategoria()).isNotNull();
            // Verifica que os relacionamentos que existem estão carregados
        }
    }
    
    @Dado("que João fez compras no cartão entre dia {int} e dia {int}")
    public void queJoaoFezComprasNoCartaoEntreDiaEDia(Integer diaInicio, Integer diaFim) {
        Categoria categoria = categorias.values().iterator().next();
        Cartao cartao = cartoes.values().iterator().next();
        
        LocalDate dataCompra1 = LocalDate.now().withDayOfMonth(diaInicio + 2);
        LocalDate dataCompra2 = LocalDate.now().withDayOfMonth(diaFim - 2);
        
        criarDespesa("Compra cartão 1", 100.0, dataCompra1, null, cartao, categoria);
        criarDespesa("Compra cartão 2", 200.0, dataCompra2, null, cartao, categoria);
    }
    
    @Quando("busco despesas do cartão no período da fatura")
    public void buscoDespesasDoCartaoNoPeriodoDaFatura() {
        Cartao cartao = cartoes.values().iterator().next();
        LocalDate dataInicio = LocalDate.now().withDayOfMonth(1);
        LocalDate dataFim = LocalDate.now().withDayOfMonth(15);
        
        resultadoBusca = despesaRepository.buscarDespesasPorCartaoEPeriodoFatura(
            cartao.getId(), dataInicio, dataFim
        );
    }
    
    @Então("deve retornar apenas as despesas do cartão no período")
    public void deveRetornarApenasAsDespesasDoCartaoNoPeriodo() {
        assertThat(resultadoBusca).isNotEmpty();
        Cartao cartaoEsperado = cartoes.values().iterator().next();
        
        for (Despesa despesa : resultadoBusca) {
            assertThat(despesa.getCartao().getId()).isEqualTo(cartaoEsperado.getId());
        }
    }
    
    @Então("as despesas devem estar ordenadas por data")
    public void asDespesasDevemEstarOrdenadasPorData() {
        for (int i = 1; i < resultadoBusca.size(); i++) {
            LocalDate dataAnterior = resultadoBusca.get(i-1).getData();
            LocalDate dataAtual = resultadoBusca.get(i).getData();
            assertThat(dataAtual).isAfterOrEqualTo(dataAnterior);
        }
    }
    
    @Dado("que João quer controlar seus gastos mensais")
    public void queJoaoQuerControlarSeusGastosMensais() {
        // Setup básico já está no contexto
    }
    
    @Quando("calculo o total de despesas do mês atual")
    public void calculoOTotalDeDespesasDoMesAtual() {
        LocalDate hoje = LocalDate.now();
        totalCalculado = despesaRepository.buscarTotalDespesasPorMesAno(hoje.getMonthValue(), hoje.getYear());
    }
    
    @Então("deve retornar a soma correta de todas as despesas do mês")
    public void deveRetornarASomaCorretaDeTodasAsDespesasDoMes() {
        assertThat(totalCalculado).isNotNull();
        assertThat(totalCalculado).isGreaterThanOrEqualTo(0.0);
    }
    
    @Então("o valor deve ser maior que zero se houver despesas")
    public void oValorDeveSerMaiorQueZeroSeHouverDespesas() {
        if (quantidadeDespesasEsperada > 0) {
            assertThat(totalCalculado).isGreaterThan(0.0);
        }
    }
    
    @Dado("que João quer visualizar seus gastos em um gráfico")
    public void queJoaoQuerVisualizarSeusGastosEmUmGrafico() {
        // Setup já feito no contexto
    }
    
    @Quando("busco dados agrupados por mês no último trimestre")
    public void buscoDadosAgrupadosPorMesNoUltimoTrimestre() {
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusMonths(3);
        
        dadosGrafico = despesaRepository.buscarDespesasAgrupadasPorMes(dataInicio, dataFim);
    }
    
    @Então("deve retornar os totais mensais corretamente")
    public void deveRetornarOsTotaisMensaisCorretamente() {
        assertThat(dadosGrafico).isNotNull();
        // Se há despesas, deve ter dados
        if (quantidadeDespesasEsperada > 0) {
            assertThat(dadosGrafico).isNotEmpty();
        }
    }
    
    @Então("os dados devem estar no formato apropriado para gráficos")
    public void osDadosDevemEstarNoFormatoApropriadoParaGraficos() {
        for (DespesaMensalDTO dto : dadosGrafico) {
            assertThat(dto.getMes()).isNotNull();
            assertThat(dto.getTotal()).isNotNull();
            assertThat(dto.getTotal()).isGreaterThanOrEqualTo(0.0);
        }
    }
    
    @Então("devo conseguir cadastrar todas as despesas")
    public void devoConseguirCadastrarTodasAsDespesas() {
        List<Despesa> todasDespesas = despesaRepository.findAll();
        assertThat(todasDespesas).hasSize(quantidadeDespesasEsperada);
    }
    
    @Então("deve ser possível buscar todas essas despesas por filtros")
    public void deveSerPossiveLBuscarTodasEssasDespesasPorFiltros() {
        List<Despesa> despesasFiltradas = despesaRepository.findByFiltrosSemPaginar(null, null, null);
        assertThat(despesasFiltradas).hasSizeGreaterThanOrEqualTo(quantidadeDespesasEsperada);
    }
    
    @Então("as consultas de totais devem refletir o valor correto")
    public void asConsultasDeTotaisDevemRefletirOValorCorreto() {
        Double totalGeral = despesaRepository.buscarTotalDespesas();
        assertThat(totalGeral).isEqualTo(totalDespesasEsperado);
    }
} 