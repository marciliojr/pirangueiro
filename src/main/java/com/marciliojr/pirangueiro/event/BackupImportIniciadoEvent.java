package com.marciliojr.pirangueiro.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Evento disparado quando uma importação de backup é iniciada
 */
@Getter
public class BackupImportIniciadoEvent extends ApplicationEvent {
    
    private final String requestId;
    private final byte[] conteudoArquivo;
    private final String nomeArquivo;
    private final LocalDateTime dataEvento;
    
    public BackupImportIniciadoEvent(Object source, String requestId, MultipartFile arquivo) throws IOException {
        super(source);
        this.requestId = requestId;
        this.conteudoArquivo = arquivo.getBytes(); // Lê o conteúdo imediatamente
        this.nomeArquivo = arquivo.getOriginalFilename();
        this.dataEvento = LocalDateTime.now();
    }
} 