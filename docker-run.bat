@echo off
REM Script para gerenciar o Docker do projeto Pirangueiro no Windows
REM Uso: docker-run.bat [comando]

setlocal enabledelayedexpansion

if "%1"=="" goto help
if "%1"=="help" goto help
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="build" goto build
if "%1"=="logs" goto logs
if "%1"=="status" goto status
if "%1"=="clean" goto clean
if "%1"=="shell" goto shell
if "%1"=="test-db" goto test_db
goto help

:help
echo.
echo === Gerenciador Docker - Projeto Pirangueiro ===
echo.
echo Comandos disponiveis:
echo   start     - Iniciar a aplicacao
echo   stop      - Parar a aplicacao
echo   restart   - Reiniciar a aplicacao
echo   build     - Rebuild da aplicacao
echo   logs      - Ver logs da aplicacao
echo   status    - Status dos containers
echo   clean     - Limpar containers e imagens
echo   shell     - Entrar no container
echo   test-db   - Testar conexao com banco
echo.
goto end

:check_mariadb
echo Verificando se MariaDB esta rodando...
netstat -an | findstr :3306 >nul 2>&1
if %errorlevel% == 0 (
    echo [OK] MariaDB esta rodando na porta 3306
    exit /b 0
) else (
    echo [AVISO] MariaDB nao foi detectado na porta 3306
    echo Certifique-se de que o banco esta rodando antes de continuar
    exit /b 1
)

:start
echo Iniciando aplicacao Pirangueiro...
call :check_mariadb
docker-compose up -d --build
if %errorlevel% == 0 (
    echo.
    echo [OK] Aplicacao iniciada com sucesso!
    echo API: http://localhost:8080/api
    echo Swagger: http://localhost:8080/api/swagger-ui.html
) else (
    echo [ERRO] Falha ao iniciar a aplicacao
)
goto end

:stop
echo Parando aplicacao...
docker-compose down
if %errorlevel% == 0 (
    echo [OK] Aplicacao parada com sucesso!
) else (
    echo [ERRO] Falha ao parar a aplicacao
)
goto end

:restart
echo Reiniciando aplicacao...
call :stop
timeout /t 3 /nobreak >nul
call :start
goto end

:build
echo Fazendo rebuild da aplicacao...
docker-compose down
docker-compose build --no-cache
docker-compose up -d
if %errorlevel% == 0 (
    echo [OK] Rebuild concluido com sucesso!
) else (
    echo [ERRO] Falha no rebuild
)
goto end

:logs
echo Mostrando logs da aplicacao...
docker-compose logs -f pirangueiro-app
goto end

:status
echo Status dos containers:
docker-compose ps
goto end

:clean
echo Limpando containers e imagens...
docker-compose down -v
docker system prune -f
echo [OK] Limpeza concluida!
goto end

:shell
echo Entrando no container...
docker exec -it pirangueiro-backend bash
goto end

:test_db
echo Testando conexao com banco de dados...
call :check_mariadb
goto end

:check_docker
where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Docker nao esta instalado ou nao esta no PATH!
    exit /b 1
)

where docker-compose >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Docker Compose nao esta instalado ou nao esta no PATH!
    exit /b 1
)
exit /b 0

:end
endlocal 