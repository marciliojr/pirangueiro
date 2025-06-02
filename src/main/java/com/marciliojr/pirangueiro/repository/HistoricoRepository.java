package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {
    
    List<Historico> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Long entidadeId);
    
} 