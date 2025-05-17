package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Grafico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GraficosRepository extends JpaRepository<Grafico, Long> {
    
    @Query("SELECT new map(c.nome as categoria, SUM(d.valor) as valor) " +
           "FROM Despesa d " +
           "JOIN d.categoria c " +
           "WHERE MONTH(d.data) = :mes " +
           "AND YEAR(d.data) = :ano " +
           "GROUP BY c.nome " +
           "ORDER BY valor DESC")
    List<Object[]> buscarDespesasPorCategoriaMesAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);

    @Query("SELECT new map(c.nome as categoria, SUM(r.valor) as valor) " +
           "FROM Receita r " +
           "JOIN r.categoria c " +
           "WHERE MONTH(r.data) = :mes " +
           "AND YEAR(r.data) = :ano " +
           "GROUP BY c.nome " +
           "ORDER BY valor DESC")
    List<Object[]> buscarReceitasPorCategoriaMesAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);

    @Query("SELECT COALESCE(SUM(d.valor), 0) " +
           "FROM Despesa d " +
           "WHERE MONTH(d.data) = :mes " +
           "AND YEAR(d.data) = :ano")
    Double buscarTotalDespesasPorMesAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);

    @Query("SELECT COALESCE(SUM(r.valor), 0) " +
           "FROM Receita r " +
           "WHERE MONTH(r.data) = :mes " +
           "AND YEAR(r.data) = :ano")
    Double buscarTotalReceitasPorMesAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);

    @Query("SELECT COALESCE(SUM(d.valor), 0) " +
           "FROM Despesa d " +
           "WHERE YEAR(d.data) = :ano " +
           "GROUP BY MONTH(d.data) " +
           "ORDER BY MONTH(d.data)")
    List<Double> buscarDespesasMensaisPorAno(@Param("ano") Integer ano);

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM Receita r")
    Double buscarTotalReceitas();

    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d")
    Double buscarTotalDespesas();
} 