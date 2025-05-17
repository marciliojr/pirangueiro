package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.LimiteGastos;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LimiteGastosRepository extends JpaRepository<LimiteGastos, Long> {
    List<LimiteGastos> findByDescricaoContainingIgnoreCase(String descricao);
} 