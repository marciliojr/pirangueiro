package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.util.List;

@Data
public class GraficoVariacaoMensalDTO {
    private Integer ano;
    private List<TotalMensalDTO> totaisMensais;
} 