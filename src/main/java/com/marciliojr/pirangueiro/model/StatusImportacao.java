package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade para controlar o status das importações de backup
 */
@Entity
@Table(name = "status_importacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusImportacao {
    
    @Id
    private String requestId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEnum status;
    
    @Column(length = 1000)
    private String mensagem;
    
    @Column(nullable = false)
    private LocalDateTime dataInicio;
    
    private LocalDateTime dataFinalizacao;
    
    private String nomeArquivo;
    
    private Integer totalRegistros;
    
    private String versaoBackup;
    
    @Column(length = 2000)
    private String detalhesErro;
    
    public enum StatusEnum {
        INICIADO,
        PROCESSANDO,
        CONCLUIDO,
        ERRO
    }
    
    public static StatusImportacao criar(String requestId, String nomeArquivo) {
        StatusImportacao status = new StatusImportacao();
        status.setRequestId(requestId);
        status.setStatus(StatusEnum.INICIADO);
        status.setMensagem("Importação iniciada");
        status.setDataInicio(LocalDateTime.now());
        status.setNomeArquivo(nomeArquivo);
        return status;
    }
    
    public void atualizar(StatusEnum novoStatus, String novaMensagem) {
        this.status = novoStatus;
        this.mensagem = novaMensagem;
        if (novoStatus == StatusEnum.CONCLUIDO || novoStatus == StatusEnum.ERRO) {
            this.dataFinalizacao = LocalDateTime.now();
        }
    }
} 