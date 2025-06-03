package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.StatusImportacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatusImportacaoRepository extends JpaRepository<StatusImportacao, String> {
    
    @Query("SELECT s FROM StatusImportacao s WHERE s.dataInicio >= :dataInicio ORDER BY s.dataInicio DESC")
    List<StatusImportacao> findByDataInicioAfterOrderByDataInicioDesc(LocalDateTime dataInicio);
    
    @Query("SELECT s FROM StatusImportacao s ORDER BY s.dataInicio DESC")
    List<StatusImportacao> findAllOrderByDataInicioDesc();
    
    @Query("SELECT s FROM StatusImportacao s WHERE s.status = :status ORDER BY s.dataInicio DESC")
    List<StatusImportacao> findByStatusOrderByDataInicioDesc(StatusImportacao.StatusEnum status);
} 