package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Receita;
import com.marciliojr.pirangueiro.model.Categoria;
import com.marciliojr.pirangueiro.dto.ReceitaMensalDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT DISTINCT r FROM Receita r " +
           "LEFT JOIN FETCH r.conta " +
           "LEFT JOIN FETCH r.categoria " +
           "WHERE MONTH(r.data) = :mes AND YEAR(r.data) = :ano",
           countQuery = "SELECT COUNT(r) FROM Receita r WHERE MONTH(r.data) = :mes AND YEAR(r.data) = :ano")
    Page<Receita> findByMesEAnoWithRelationshipsPaged(
            @Param("mes") int mes,
            @Param("ano") int ano,
            Pageable pageable);
    
    // Métodos existentes mantidos para compatibilidade
    List<Receita> findByDescricaoContainingIgnoreCase(String descricao);

    @Query("SELECT r FROM Receita r WHERE MONTH(r.data) = :mes AND YEAR(r.data) = :ano")
    List<Receita> findByMesEAno(@Param("mes") int mes, @Param("ano") int ano);

    List<Receita> findByCategoria(Categoria categoria);

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM Receita r")
    Double buscarTotalReceitas();

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM Receita r WHERE MONTH(r.data) = :mes AND YEAR(r.data) = :ano")
    Double buscarTotalReceitasPorMesAno(@Param("mes") Integer mes, @Param("ano") Integer ano);

    @Query(value = "SELECT DISTINCT r FROM Receita r " +
           "LEFT JOIN FETCH r.conta " +
           "LEFT JOIN FETCH r.categoria " +
           "WHERE (:descricao IS NULL OR LOWER(r.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))) " +
           "AND (:mes IS NULL OR MONTH(r.data) = :mes) " +
           "AND (:ano IS NULL OR YEAR(r.data) = :ano)",
           countQuery = "SELECT COUNT(DISTINCT r) FROM Receita r " +
           "WHERE (:descricao IS NULL OR LOWER(r.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))) " +
           "AND (:mes IS NULL OR MONTH(r.data) = :mes) " +
           "AND (:ano IS NULL OR YEAR(r.data) = :ano)")
    Page<Receita> findByFiltros(
            @Param("descricao") String descricao,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            Pageable pageable);

    // Query para gráfico: receitas agrupadas por mês
    @Query("SELECT YEAR(r.data) as ano, MONTH(r.data) as mes, COALESCE(SUM(r.valor), 0.0) as total " +
           "FROM Receita r " +
           "WHERE r.data BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY YEAR(r.data), MONTH(r.data) " +
           "ORDER BY YEAR(r.data), MONTH(r.data)")
    List<Object[]> buscarReceitasAgrupadasPorMesRaw(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    // Query simplificada para gráfico: receitas agrupadas por mês
    @Query("SELECT new com.marciliojr.pirangueiro.dto.ReceitaMensalDTO(" +
           "CONCAT(CAST(YEAR(r.data) AS string), '-', " +
           "CASE WHEN MONTH(r.data) < 10 THEN CONCAT('0', CAST(MONTH(r.data) AS string)) " +
           "ELSE CAST(MONTH(r.data) AS string) END), " +
           "COALESCE(SUM(r.valor), 0.0)) " +
           "FROM Receita r " +
           "WHERE r.data BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY YEAR(r.data), MONTH(r.data) " +
           "ORDER BY YEAR(r.data), MONTH(r.data)")
    List<ReceitaMensalDTO> buscarReceitasAgrupadasPorMes(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);
} 