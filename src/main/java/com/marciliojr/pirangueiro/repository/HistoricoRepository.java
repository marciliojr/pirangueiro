package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Historico;
import com.marciliojr.pirangueiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {
    
    List<Historico> findByUsuarioOrderByDataHoraDesc(Usuario usuario);
    
    List<Historico> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Long entidadeId);
    
    @Query("SELECT h FROM Historico h WHERE h.usuario.id = :usuarioId AND h.dataHora BETWEEN :dataInicio AND :dataFim ORDER BY h.dataHora DESC")
    List<Historico> findByUsuarioAndPeriodo(@Param("usuarioId") Long usuarioId, 
                                          @Param("dataInicio") LocalDateTime dataInicio, 
                                          @Param("dataFim") LocalDateTime dataFim);
} 