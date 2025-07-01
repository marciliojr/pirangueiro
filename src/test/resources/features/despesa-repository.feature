# language: pt
Funcionalidade: Repository de Despesas - Gerenciamento financeiro de um trabalhador

  Contexto: Trabalhador João com salário de R$ 1000
    Dado que existe um trabalhador chamado "João"
    E ele possui uma conta corrente "Conta Salário"
    E ele possui um cartão de crédito "Cartão Básico" com limite de R$ 500
    E existem as seguintes categorias de despesa:
      | nome           | cor      |
      | Alimentação    | #FF5722  |
      | Transporte     | #2196F3  |
      | Moradia        | #4CAF50  |
      | Saúde          | #E91E63  |
      | Lazer          | #9C27B0  |

  Cenário: Trabalhador com despesas básicas mensais - cenário positivo
    Quando João registra as seguintes despesas no mês atual:
      | descricao         | valor | categoria     | conta         | cartao |
      | Almoço no trabalho| 15.00 | Alimentação   | Conta Salário |        |
      | Passagem de ônibus| 4.50  | Transporte    | Conta Salário |        |
      | Conta de luz      | 80.00 | Moradia       | Conta Salário |        |
      | Supermercado      | 120.00| Alimentação   |               | Cartão Básico |
    Então devo conseguir buscar todas as despesas com relacionamentos
    E devo encontrar 4 despesas cadastradas
    E o total de despesas deve ser R$ 219.50
    E devo conseguir buscar despesas por categoria "Alimentação"
    E deve retornar 2 despesas da categoria "Alimentação"

  Cenário: Busca de despesas por descrição
    Dado que João tem despesas cadastradas no sistema
    Quando busco despesas pela descrição "almoço"
    Então deve retornar despesas que contenham "almoço" na descrição
    E as despesas retornadas devem ter seus relacionamentos carregados

  Cenário: Filtro de despesas por mês e ano
    Dado que João tem despesas em diferentes meses
    Quando busco despesas do mês atual
    Então deve retornar apenas as despesas do mês atual
    E cada despesa deve ter conta, categoria e cartão carregados

  Cenário: Busca específica por conta, cartão e data
    Dado que João tem múltiplas despesas
    Quando busco despesas específicas por:
      | campo     | valor         |
      | descricao | Supermercado  |
      | conta     | Conta Salário |
      | data      | hoje          |
    Então deve retornar apenas as despesas que atendem aos critérios
    E as despesas devem ter todos os relacionamentos carregados

  Cenário: Despesas no cartão de crédito por período
    Dado que João fez compras no cartão entre dia 1 e dia 15
    Quando busco despesas do cartão no período da fatura
    Então deve retornar apenas as despesas do cartão no período
    E as despesas devem estar ordenadas por data

  Cenário: Total de despesas mensais para controle financeiro
    Dado que João quer controlar seus gastos mensais
    Quando calculo o total de despesas do mês atual
    Então deve retornar a soma correta de todas as despesas do mês
    E o valor deve ser maior que zero se houver despesas

  Cenário: Dados para gráfico de despesas mensais
    Dado que João quer visualizar seus gastos em um gráfico
    Quando busco dados agrupados por mês no último trimestre
    Então deve retornar os totais mensais corretamente
    E os dados devem estar no formato apropriado para gráficos

  Cenário: Trabalhador gastando além do orçamento - cenário negativo
    Quando João registra despesas excessivas:
      | descricao           | valor  | categoria     |
      | Conta de cartão     | 800.00 | Moradia       |
      | Financiamento carro | 350.00 | Transporte    |
      | Plano de saúde      | 200.00 | Saúde         |
    Então devo conseguir cadastrar todas as despesas
    E o total de despesas deve ser R$ 1350.00
    E deve ser possível buscar todas essas despesas por filtros
    E as consultas de totais devem refletir o valor correto 