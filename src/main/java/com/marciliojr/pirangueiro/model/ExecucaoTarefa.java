package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class ExecucaoTarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeTarefa;

    @Column(nullable = false)
    private LocalDate dataExecucao;

    public static ExecucaoTarefa criar(String nomeTarefa) {
        ExecucaoTarefa execucao = new ExecucaoTarefa();
        execucao.setNomeTarefa(nomeTarefa);
        execucao.setDataExecucao(LocalDate.now());
        return execucao;
    }
} 