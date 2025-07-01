package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.HistoricoDTO;
import com.marciliojr.pirangueiro.model.Historico;
import com.marciliojr.pirangueiro.service.HistoricoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsável por gerenciar o histórico de operações do sistema.
 * 
 * <p>Este controller fornece endpoints para consultar o histórico de operações
 * realizadas no sistema, incluindo funcionalidades como:</p>
 * <ul>
 *   <li>Auditoria de operações CRUD</li>
 * </ul>
 * 
 * <p>O histórico é fundamental para auditoria e rastreabilidade das operações
 * realizadas no sistema, permitindo identificar o que foi feito e quando.</p>
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Histórico de Operações", description = "APIs para consulta do histórico de operações do sistema")
@RestController
@RequestMapping("/api/historicos")
public class HistoricoController {

    /**
     * Serviço responsável pela lógica de negócio do histórico.
     */
    @Autowired
    private HistoricoService historicoService;

    /**
     * Converte uma entidade Historico para HistoricoDTO.
     * 
     * @param historico Entidade Historico a ser convertida
     * @return HistoricoDTO correspondente
     */
    private HistoricoDTO convertToDTO(Historico historico) {
        HistoricoDTO dto = new HistoricoDTO();
        dto.setId(historico.getId());
        dto.setTipoOperacao(historico.getTipoOperacao());
        dto.setEntidade(historico.getEntidade());
        dto.setEntidadeId(historico.getEntidadeId());
        dto.setInfo(historico.getInfo());
        dto.setDataHora(historico.getDataHora());
        dto.setUsuarioId(historico.getUsuario() != null ? historico.getUsuario().getId() : null);
        dto.setUsuarioNome(historico.getUsuario() != null ? historico.getUsuario().getNome() : null);
        return dto;
    }
} 