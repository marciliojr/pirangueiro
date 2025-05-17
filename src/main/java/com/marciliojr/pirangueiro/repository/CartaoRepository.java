package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartaoRepository extends JpaRepository<Cartao, Long> {
    List<Cartao> findByNomeContainingIgnoreCase(String nome);
} 