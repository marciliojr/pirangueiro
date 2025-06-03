package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para backup da entidade Despesa.
 * Mantém referências por ID para os relacionamentos.
 */
@Data
public class DespesaBackupDTO {
    private Long id;
    private String descricao;
    private Double valor;
    private LocalDate data;
    private Long contaId; // Referência para Conta
    private Long cartaoId; // Referência para Cartao
    private Long categoriaId; // Referência para Categoria
    private String anexo;
    private String observacao;
    private Integer numeroParcela;
    private Integer totalParcelas;
    private Boolean pago;
} 