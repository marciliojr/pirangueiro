package com.marciliojr.pirangueiro.event;

import com.marciliojr.pirangueiro.dto.BackupDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Evento disparado quando uma importação de backup é finalizada (com sucesso ou erro)
 */
@Getter
public class BackupImportFinalizadoEvent extends ApplicationEvent {
    
    private final String requestId;
    private final boolean sucesso;
    private final String mensagem;
    private final BackupDTO backup;
    private final LocalDateTime dataEvento;
    private final Exception erro;
    
    public BackupImportFinalizadoEvent(Object source, String requestId, boolean sucesso, 
                                      String mensagem, BackupDTO backup) {
        this(source, requestId, sucesso, mensagem, backup, null);
    }
    
    public BackupImportFinalizadoEvent(Object source, String requestId, boolean sucesso, 
                                      String mensagem, BackupDTO backup, Exception erro) {
        super(source);
        this.requestId = requestId;
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.backup = backup;
        this.dataEvento = LocalDateTime.now();
        this.erro = erro;
    }
} 