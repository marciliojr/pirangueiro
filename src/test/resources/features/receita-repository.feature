# language: pt
Funcionalidade: Repository de Receitas - Gestão de renda de um trabalhador

  Contexto: Trabalhador João com diferentes fontes de renda
    Dado que existe um trabalhador chamado "João"
    E ele possui uma conta corrente "Conta Salário"
    E ele possui uma conta poupança "Poupança"
    E existem as seguintes categorias de receita:
      | nome           | cor      |
      | Salário        | #4CAF50  |
      | Freelance      | #FF9800  |
      | Investimentos  | #3F51B5  |
      | Extras         | #795548  |

  Cenário: Trabalhador com renda regular mensal - cenário positivo
    Quando João registra as seguintes receitas no mês atual:
      | descricao                | valor  | categoria     | conta         |
      | Salário CLT             | 1000.00| Salário       | Conta Salário |
      | Freelance programação   | 300.00 | Freelance     | Conta Salário |
      | Rendimento poupança     | 15.00  | Investimentos | Poupança      |
      | Venda de item usado     | 50.00  | Extras        | Conta Salário |
    Então devo conseguir buscar todas as receitas com relacionamentos
    E devo encontrar 4 receitas cadastradas
    E o total de receitas deve ser R$ 1365.00
    E devo conseguir buscar receitas por categoria "Salário"
    E deve retornar 1 receita da categoria "Salário"

  Cenário: Busca de receitas por descrição
    Dado que João tem receitas cadastradas no sistema
    Quando busco receitas pela descrição "salário"
    Então deve retornar receitas que contenham "salário" na descrição
    E as receitas retornadas devem ter seus relacionamentos carregados

  Cenário: Filtro de receitas por mês e ano
    Dado que João tem receitas em diferentes meses
    Quando busco receitas do mês atual
    Então deve retornar apenas as receitas do mês atual
    E cada receita deve ter conta e categoria carregadas

  Cenário: Receitas por ID com relacionamentos
    Dado que João tem uma receita específica cadastrada
    Quando busco a receita por ID com relacionamentos
    Então deve retornar a receita com conta e categoria carregadas
    E todos os dados da receita devem estar corretos

  Cenário: Busca paginada de receitas por filtros
    Dado que João tem múltiplas receitas cadastradas
    Quando busco receitas com filtros usando paginação:
      | descrição | mês | ano |
      | freelance | 12  | 2024|
    Então deve retornar as receitas que atendem aos filtros
    E o resultado deve estar paginado corretamente
    E as receitas devem ter relacionamentos carregados

  Cenário: Total de receitas mensais para controle financeiro
    Dado que João quer acompanhar sua renda mensal
    Quando calculo o total de receitas do mês atual
    Então deve retornar a soma correta de todas as receitas do mês
    E o valor deve ser maior que zero se houver receitas

  Cenário: Dados para gráfico de receitas mensais
    Dado que João quer visualizar sua renda em um gráfico
    Quando busco dados de receitas agrupados por mês no último trimestre
    Então deve retornar os totais mensais de receitas corretamente
    E os dados devem estar no formato DTO apropriado para gráficos

  Cenário: Trabalhador com baixa renda mensal - cenário negativo
    Quando João registra apenas receitas mínimas:
      | descricao           | valor  | categoria |
      | Trabalho esporádico | 200.00 | Freelance |
      | Ajuda familiar      | 150.00 | Extras    |
    Então devo conseguir cadastrar todas as receitas
    E o total de receitas deve ser R$ 350.00
    E deve ser possível buscar essas receitas por filtros
    E as consultas devem retornar valores corretos mesmo com renda baixa

  Cenário: Mês sem receitas - cenário limite
    Dado que não existem receitas cadastradas para um mês específico
    Quando busco receitas desse mês
    Então deve retornar uma lista vazia
    E o total de receitas do mês deve ser R$ 0.00
    E as consultas para gráficos devem tratar corretamente a ausência de dados

  Cenário: Receitas de diferentes categorias para análise
    Dado que João tem receitas de múltiplas categorias
    Quando busco receitas por categoria específica
    Então deve retornar apenas receitas da categoria solicitada
    E cada receita deve manter seus relacionamentos
    E a busca deve ser insensível a maiúsculas e minúsculas 