package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para backup da entidade Receita.
 * Mantém referências por ID para os relacionamentos.
 */
@Data
public class ReceitaBackupDTO {
    private Long id;
    private String descricao;
    private Double valor;
    private LocalDate data;
    private Long contaId; // Referência para Conta
    private Long categoriaId; // Referência para Categoria
    private String anexo;
    private String observacao;
} 