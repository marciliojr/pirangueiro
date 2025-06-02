package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DespesaDTO {
    private Long id;
    private String descricao;
    private Double valor;
    private LocalDate data;
    private ContaDTO conta;
    private CartaoDTO cartao;
    private CategoriaDTO categoria;
    private String anexo;
    private String observacao;
    private Integer quantidadeParcelas;
    private Integer numeroParcela;
    private Integer totalParcelas;
    private Boolean pago;
} 