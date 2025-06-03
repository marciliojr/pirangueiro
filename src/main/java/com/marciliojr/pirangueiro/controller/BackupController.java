package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.BackupDTO;
import com.marciliojr.pirangueiro.event.BackupImportIniciadoEvent;
import com.marciliojr.pirangueiro.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller REST para funcionalidades de backup e restauração.
 */
@RestController
@RequestMapping("api/backup")
@CrossOrigin(origins = "*")
public class BackupController {

    @Autowired
    private BackupService backupService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Endpoint para exportar todos os dados do sistema em um arquivo JSON.
     * 
     * GET /backup/export
     * 
     * @return Arquivo JSON com backup completo
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportarBackup() {
        try {
            // Gerar backup completo
            BackupDTO backup = backupService.gerarBackupCompleto();
            
            // Serializar para JSON
            String jsonBackup = backupService.serializarBackup(backup);
            
            // Criar resource para download
            ByteArrayResource resource = new ByteArrayResource(jsonBackup.getBytes("UTF-8"));
            
            // Gerar nome do arquivo com timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivo = "backup_pirangueiro_" + timestamp + ".json";
            
            // Configurar headers para download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeArquivo);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Endpoint para obter informações sobre o backup sem fazer download.
     * 
     * GET /backup/info
     * 
     * @return Informações resumidas sobre o backup atual
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> obterInfoBackup() {
        try {
            BackupDTO backup = backupService.gerarBackupCompleto();
            
            Map<String, Object> info = new HashMap<>();
            info.put("dataGeracao", backup.getDataGeracao());
            info.put("versao", backup.getVersao());
            info.put("sistemaVersao", backup.getSistemaVersao());
            info.put("totalRegistros", backup.getTotalRegistros());
            
            // Estatísticas por entidade
            Map<String, Integer> estatisticas = new HashMap<>();
            estatisticas.put("usuarios", backup.getUsuarios() != null ? backup.getUsuarios().size() : 0);
            estatisticas.put("categorias", backup.getCategorias() != null ? backup.getCategorias().size() : 0);
            estatisticas.put("contas", backup.getContas() != null ? backup.getContas().size() : 0);
            estatisticas.put("cartoes", backup.getCartoes() != null ? backup.getCartoes().size() : 0);
            estatisticas.put("pensamentos", backup.getPensamentos() != null ? backup.getPensamentos().size() : 0);
            estatisticas.put("limitesGastos", backup.getLimitesGastos() != null ? backup.getLimitesGastos().size() : 0);
            estatisticas.put("graficos", backup.getGraficos() != null ? backup.getGraficos().size() : 0);
            estatisticas.put("execucoesTarefas", backup.getExecucoesTarefas() != null ? backup.getExecucoesTarefas().size() : 0);
            estatisticas.put("despesas", backup.getDespesas() != null ? backup.getDespesas().size() : 0);
            estatisticas.put("receitas", backup.getReceitas() != null ? backup.getReceitas().size() : 0);
            estatisticas.put("notificacoes", backup.getNotificacoes() != null ? backup.getNotificacoes().size() : 0);
            estatisticas.put("historicos", backup.getHistoricos() != null ? backup.getHistoricos().size() : 0);
            
            info.put("estatisticas", estatisticas);
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao obter informações do backup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Endpoint para importar e restaurar dados a partir de um arquivo JSON.
     * 
     * POST /backup/import
     * 
     * ATENÇÃO: Este endpoint apaga TODOS os dados existentes no sistema
     * e os substitui pelos dados do arquivo de backup!
     * 
     * Agora funciona de forma ASSÍNCRONA - retorna imediatamente com um ID
     * para acompanhar o progresso da operação.
     * 
     * @param arquivo Arquivo JSON contendo o backup
     * @return ID da operação para acompanhamento do status
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importarBackup(
            @RequestParam("arquivo") MultipartFile arquivo) {
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Validações básicas
            if (arquivo.isEmpty()) {
                resultado.put("sucesso", false);
                resultado.put("erro", "Arquivo não pode estar vazio");
                return ResponseEntity.badRequest().body(resultado);
            }
            
            if (!arquivo.getOriginalFilename().toLowerCase().endsWith(".json")) {
                resultado.put("sucesso", false);
                resultado.put("erro", "Arquivo deve ter extensão .json");
                return ResponseEntity.badRequest().body(resultado);
            }
            
            // Gerar ID único para acompanhar a operação
            String requestId = UUID.randomUUID().toString();
            
            // Publicar evento para processamento assíncrono
            // O evento irá ler o conteúdo do arquivo imediatamente
            eventPublisher.publishEvent(
                new BackupImportIniciadoEvent(this, requestId, arquivo)
            );
            
            // Retorna imediatamente com ID de acompanhamento
            resultado.put("sucesso", true);
            resultado.put("mensagem", "Importação iniciada com sucesso! Use o endpoint /import/status/{requestId} para acompanhar o progresso.");
            resultado.put("requestId", requestId);
            resultado.put("status", "INICIADO");
            resultado.put("dataInicio", LocalDateTime.now());
            resultado.put("nomeArquivo", arquivo.getOriginalFilename());
            resultado.put("urlStatus", "/api/backup/import/status/" + requestId);
            
            return ResponseEntity.accepted().body(resultado);
            
        } catch (IOException e) {
            resultado.put("sucesso", false);
            resultado.put("erro", "Erro ao ler arquivo: " + e.getMessage());
            return ResponseEntity.badRequest().body(resultado);
            
        } catch (Exception e) {
            resultado.put("sucesso", false);
            resultado.put("erro", "Erro ao iniciar importação: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultado);
        }
    }

    /**
     * Endpoint para verificar o status de uma importação específica.
     * 
     * GET /backup/import/status/{requestId}
     * 
     * @param requestId ID da operação de importação
     * @return Status atual da importação
     */
    @GetMapping("/import/status/{requestId}")
    public ResponseEntity<Map<String, Object>> verificarStatusImport(@PathVariable String requestId) {
        try {
            Map<String, Object> status = backupService.obterStatusImportacao(requestId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao verificar status: " + e.getMessage());
            erro.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Endpoint para listar importações recentes (últimas 24 horas).
     * 
     * GET /backup/import/history
     * 
     * @return Lista de importações recentes
     */
    @GetMapping("/import/history")
    public ResponseEntity<Map<String, Object>> listarHistoricoImportacoes() {
        try {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("importacoesRecentes", backupService.listarImportacoesRecentes());
            resposta.put("dataConsulta", LocalDateTime.now());
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao listar histórico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Endpoint para listar todas as importações registradas.
     * 
     * GET /backup/import/history/all
     * 
     * @return Lista de todas as importações
     */
    @GetMapping("/import/history/all")
    public ResponseEntity<Map<String, Object>> listarTodasImportacoes() {
        try {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("todasImportacoes", backupService.listarTodasImportacoes());
            resposta.put("dataConsulta", LocalDateTime.now());
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao listar todas as importações: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    /**
     * Endpoint para limpar registros de status antigos.
     * 
     * DELETE /backup/import/cleanup
     * 
     * @return Resultado da limpeza
     */
    @DeleteMapping("/import/cleanup")
    public ResponseEntity<Map<String, Object>> limparStatusAntigos() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            backupService.limparStatusAntigos();
            
            resultado.put("sucesso", true);
            resultado.put("mensagem", "Registros de status antigos removidos com sucesso");
            resultado.put("dataLimpeza", LocalDateTime.now());
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            resultado.put("sucesso", false);
            resultado.put("erro", "Erro ao limpar status antigos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultado);
        }
    }

    /**
     * Endpoint para limpar todos os dados do sistema.
     * 
     * DELETE /backup/clear
     * 
     * ATENÇÃO: Este endpoint apaga TODOS os dados do sistema!
     * Use com extrema cautela!
     * 
     * @return Resultado da operação
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> limparSistema() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            backupService.limparTodasTabelas();
            
            resultado.put("sucesso", true);
            resultado.put("mensagem", "Todos os dados foram removidos do sistema");
            resultado.put("dataLimpeza", LocalDateTime.now());
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            resultado.put("sucesso", false);
            resultado.put("erro", "Erro ao limpar sistema: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultado);
        }
    }

    /**
     * Endpoint para testar conectividade e funcionamento do serviço de backup.
     * 
     * GET /backup/status
     * 
     * @return Status do serviço
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> verificarStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // Testar geração de backup (sem salvar)
            BackupDTO backup = backupService.gerarBackupCompleto();
            
            status.put("status", "OK");
            status.put("servicoAtivo", true);
            status.put("versaoServico", "1.0");
            status.put("totalRegistrosDisponiveis", backup.getTotalRegistros());
            status.put("dataVerificacao", LocalDateTime.now());
            status.put("funcionalidades", Map.of(
                "exportacao", "Disponível",
                "importacaoAssincrona", "Disponível",
                "notificacaoEmail", "Disponível",
                "acompanhamentoStatus", "Disponível"
            ));
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("status", "ERRO");
            status.put("servicoAtivo", false);
            status.put("erro", e.getMessage());
            status.put("dataVerificacao", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(status);
        }
    }
} 