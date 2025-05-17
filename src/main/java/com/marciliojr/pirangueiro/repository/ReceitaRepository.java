package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Receita;
import com.marciliojr.pirangueiro.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {
    
    // Query com fetch join para obter todas as receitas com seus relacionamentos
    @Query("SELECT r FROM Receita r LEFT JOIN FETCH r.conta LEFT JOIN FETCH r.categoria")
    List<Receita> findAllWithRelationships();
    
    // Query com fetch join para buscar receita por id com seus relacionamentos
    @Query("SELECT r FROM Receita r LEFT JOIN FETCH r.conta LEFT JOIN FETCH r.categoria WHERE r.id = :id")
    Optional<Receita> findByIdWithRelationships(@Param("id") Long id);
    
    // Query com fetch join para buscar por descrição com relacionamentos
    @Query("SELECT r FROM Receita r LEFT JOIN FETCH r.conta LEFT JOIN FETCH r.categoria WHERE LOWER(r.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))")
    List<Receita> findByDescricaoContainingWithRelationships(@Param("descricao") String descricao);
    
    // Query com fetch join para buscar por mês e ano com relacionamentos
    @Query("SELECT r FROM Receita r LEFT JOIN FETCH r.conta LEFT JOIN FETCH r.categoria WHERE MONTH(r.data) = :mes AND YEAR(r.data) = :ano")
    List<Receita> findByMesEAnoWithRelationships(@Param("mes") int mes, @Param("ano") int ano);
    
    // Métodos existentes mantidos para compatibilidade
    List<Receita> findByDescricaoContainingIgnoreCase(String descricao);

    @Query("SELECT r FROM Receita r WHERE MONTH(r.data) = :mes AND YEAR(r.data) = :ano")
    List<Receita> findByMesEAno(@Param("mes") int mes, @Param("ano") int ano);

    List<Receita> findByCategoria(Categoria categoria);

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM Receita r")
    Double buscarTotalReceitas();
} 