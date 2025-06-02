package com.marciliojr.pirangueiro.dto;

import com.marciliojr.pirangueiro.model.Historico;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistoricoDTO {
    private Long id;
    private Historico.TipoOperacao tipoOperacao;
    private String entidade;
    private Long entidadeId;
    private LocalDateTime dataHora;
} 