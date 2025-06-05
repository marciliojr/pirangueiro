#!/bin/bash

# Script para gerenciar o Docker do projeto Pirangueiro
# Uso: ./docker-run.sh [comando]

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

function print_help() {
    echo -e "${BLUE}=== Gerenciador Docker - Projeto Pirangueiro ===${NC}"
    echo ""
    echo "Comandos disponíveis:"
    echo -e "  ${GREEN}start${NC}     - Iniciar a aplicação"
    echo -e "  ${GREEN}stop${NC}      - Parar a aplicação"
    echo -e "  ${GREEN}restart${NC}   - Reiniciar a aplicação"
    echo -e "  ${GREEN}build${NC}     - Rebuild da aplicação"
    echo -e "  ${GREEN}logs${NC}      - Ver logs da aplicação"
    echo -e "  ${GREEN}status${NC}    - Status dos containers"
    echo -e "  ${GREEN}clean${NC}     - Limpar containers e imagens"
    echo -e "  ${GREEN}shell${NC}     - Entrar no container"
    echo -e "  ${GREEN}test-db${NC}   - Testar conexão com banco"
    echo ""
}

function check_mariadb() {
    echo -e "${YELLOW}Verificando se MariaDB está rodando...${NC}"
    if command -v mysql &> /dev/null; then
        if mysql -u root -p321 -e "SELECT 1;" &> /dev/null; then
            echo -e "${GREEN}✓ MariaDB está rodando e acessível${NC}"
            return 0
        else
            echo -e "${RED}✗ MariaDB não está acessível com as credenciais configuradas${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}! Cliente MySQL não encontrado, assumindo que o banco está rodando${NC}"
        return 0
    fi
}

function docker_start() {
    echo -e "${BLUE}Iniciando aplicação Pirangueiro...${NC}"
    check_mariadb
    docker-compose up -d --build
    echo -e "${GREEN}✓ Aplicação iniciada!${NC}"
    echo -e "API: http://localhost:8080/api"
    echo -e "Swagger: http://localhost:8080/api/swagger-ui.html"
}

function docker_stop() {
    echo -e "${YELLOW}Parando aplicação...${NC}"
    docker-compose down
    echo -e "${GREEN}✓ Aplicação parada!${NC}"
}

function docker_restart() {
    docker_stop
    sleep 2
    docker_start
}

function docker_build() {
    echo -e "${BLUE}Fazendo rebuild da aplicação...${NC}"
    docker-compose down
    docker-compose build --no-cache
    docker-compose up -d
    echo -e "${GREEN}✓ Rebuild concluído!${NC}"
}

function docker_logs() {
    echo -e "${BLUE}Mostrando logs da aplicação...${NC}"
    docker-compose logs -f pirangueiro-app
}

function docker_status() {
    echo -e "${BLUE}Status dos containers:${NC}"
    docker-compose ps
}

function docker_clean() {
    echo -e "${YELLOW}Limpando containers e imagens...${NC}"
    docker-compose down -v
    docker system prune -f
    echo -e "${GREEN}✓ Limpeza concluída!${NC}"
}

function docker_shell() {
    echo -e "${BLUE}Entrando no container...${NC}"
    docker exec -it pirangueiro-backend bash
}

function test_database() {
    echo -e "${BLUE}Testando conexão com banco de dados...${NC}"
    check_mariadb
}

# Verificar se docker e docker-compose estão instalados
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker não está instalado!${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Docker Compose não está instalado!${NC}"
    exit 1
fi

# Processar comando
case "${1:-help}" in
    start)
        docker_start
        ;;
    stop)
        docker_stop
        ;;
    restart)
        docker_restart
        ;;
    build)
        docker_build
        ;;
    logs)
        docker_logs
        ;;
    status)
        docker_status
        ;;
    clean)
        docker_clean
        ;;
    shell)
        docker_shell
        ;;
    test-db)
        test_database
        ;;
    help|*)
        print_help
        ;;
esac 