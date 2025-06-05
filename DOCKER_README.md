# 🐳 Guia Docker - Projeto Pirangueiro

## 📋 Pré-requisitos

1. **Docker** e **Docker Compose** instalados
2. **MariaDB** rodando na sua máquina local na porta 3306
3. Banco de dados `pirangueiro` criado

## 🚀 Como executar

### 1. Preparar o ambiente

Certifique-se de que o MariaDB está rodando:
```bash
# Verificar se o MariaDB está rodando
netstat -an | findstr 3306
```

### 2. Construir e executar

```bash
# Construir e executar em modo detached
docker-compose up -d --build

# Ou executar em foreground para ver os logs
docker-compose up --build
```

### 3. Verificar se está funcionando

- **API**: http://localhost:8080/api
- **Swagger**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/api-docs

## 🌐 Acesso do Frontend

### URLs base para o frontend acessar:

```javascript
// URL base da API
const API_BASE_URL = 'http://localhost:8080/api'

// Exemplos de endpoints
const endpoints = {
  // Substitua pelos seus endpoints reais
  despesas: `${API_BASE_URL}/despesas`,
  usuarios: `${API_BASE_URL}/usuarios`,
  relatorios: `${API_BASE_URL}/relatorios`
}
```

### Configuração CORS

O projeto já está configurado para aceitar requisições de qualquer origem quando rodando no Docker. As seguintes configurações foram aplicadas:

- `allowed-origins: *` - Permite qualquer origem
- `allowed-methods: GET,POST,PUT,DELETE,OPTIONS,PATCH` - Métodos HTTP permitidos
- `allowed-headers: *` - Qualquer header permitido
- `allow-credentials: true` - Permite envio de credentials

### Exemplo de requisição do Frontend

```javascript
// Exemplo com fetch
async function buscarDespesas() {
  try {
    const response = await fetch('http://localhost:8080/api/despesas', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        // Adicione headers de autenticação se necessário
      }
    });
    
    if (response.ok) {
      const dados = await response.json();
      return dados;
    }
  } catch (error) {
    console.error('Erro ao buscar despesas:', error);
  }
}

// Exemplo com axios
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Usar a instância
const despesas = await api.get('/despesas');
```

## 🔧 Comandos úteis

```bash
# Ver logs da aplicação
docker-compose logs -f pirangueiro-app

# Parar os containers
docker-compose down

# Parar e remover volumes
docker-compose down -v

# Rebuild sem cache
docker-compose build --no-cache

# Entrar no container
docker exec -it pirangueiro-backend bash

# Ver status dos containers
docker-compose ps
```

## 🔍 Troubleshooting

### Problema: Container não consegue conectar ao banco

**Solução**: Verifique se o MariaDB está rodando e acessível. Use `host.docker.internal` ao invés de `localhost`.

### Problema: Frontend não consegue acessar a API

**Soluções**:
1. Verifique se a aplicação está rodando: http://localhost:8080/api
2. Confirme se o CORS está configurado corretamente
3. Verifique se não há firewall bloqueando a porta 8080

### Problema: Erro de build do Docker

**Soluções**:
1. Limpe o cache do Docker: `docker system prune -a`
2. Verifique se há espaço em disco suficiente
3. Rebuild sem cache: `docker-compose build --no-cache`

## 🌍 Variáveis de Ambiente

Você pode criar um arquivo `.env` na raiz do projeto para personalizar as configurações:

```env
EMAIL_USERNAME=seu_email@gmail.com
EMAIL_PASSWORD=sua_senha_de_app
RELATORIO_EMAIL_DESTINATARIO=destinatario@exemplo.com
RELATORIO_EMAIL_REMETENTE=remetente@exemplo.com
RELATORIO_EMAIL_ENABLED=false
```

## 📊 Monitoramento

### Health Check
A aplicação tem um health check configurado que verifica se está respondendo corretamente.

### Logs
Para monitorar a aplicação em tempo real:
```bash
docker-compose logs -f
```

## 🔄 Atualizações

Para atualizar a aplicação após mudanças no código:

```bash
# Parar, rebuild e restart
docker-compose down
docker-compose up --build -d
``` 