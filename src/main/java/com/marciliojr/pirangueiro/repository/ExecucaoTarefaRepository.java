package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.ExecucaoTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface ExecucaoTarefaRepository extends JpaRepository<ExecucaoTarefa, Long> {
    Optional<ExecucaoTarefa> findByNomeTarefaAndDataExecucao(String nomeTarefa, LocalDate dataExecucao);
} 