package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Despesa;
import com.marciliojr.pirangueiro.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    
    // Query com fetch join para obter todas as despesas com seus relacionamentos
    @Query("SELECT d FROM Despesa d LEFT JOIN FETCH d.conta LEFT JOIN FETCH d.categoria LEFT JOIN FETCH d.cartao")
    List<Despesa> findAllWithRelationships();
    
    // Query com fetch join para buscar despesa por id com seus relacionamentos
    @Query("SELECT d FROM Despesa d LEFT JOIN FETCH d.conta LEFT JOIN FETCH d.categoria LEFT JOIN FETCH d.cartao WHERE d.id = :id")
    Optional<Despesa> findByIdWithRelationships(@Param("id") Long id);
    
    // Query com fetch join para buscar por descrição com relacionamentos
    @Query("SELECT d FROM Despesa d LEFT JOIN FETCH d.conta LEFT JOIN FETCH d.categoria LEFT JOIN FETCH d.cartao WHERE LOWER(d.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))")
    List<Despesa> findByDescricaoContainingWithRelationships(@Param("descricao") String descricao);
    
    // Query com fetch join para buscar por mês e ano com relacionamentos
    @Query("SELECT d FROM Despesa d LEFT JOIN FETCH d.conta LEFT JOIN FETCH d.categoria LEFT JOIN FETCH d.cartao WHERE MONTH(d.data) = :mes AND YEAR(d.data) = :ano")
    List<Despesa> findByMesEAnoWithRelationships(@Param("mes") int mes, @Param("ano") int ano);
    
    // Métodos existentes mantidos para compatibilidade
    List<Despesa> findByDescricaoContainingIgnoreCase(String descricao);

    @Query("SELECT d FROM Despesa d WHERE MONTH(d.data) = :mes AND YEAR(d.data) = :ano")
    List<Despesa> findByMesEAno(@Param("mes") int mes, @Param("ano") int ano);

    List<Despesa> findByCategoria(Categoria categoria);

    @Query("SELECT d FROM Despesa d " +
           "LEFT JOIN FETCH d.conta c " +
           "LEFT JOIN FETCH d.cartao cc " +
           "LEFT JOIN FETCH d.categoria " +
           "WHERE (:descricao IS NULL OR LOWER(d.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))) " +
           "AND (:contaId IS NULL OR c.id = :contaId) " +
           "AND (:cartaoId IS NULL OR cc.id = :cartaoId) " +
           "AND (:data IS NULL OR d.data = :data)")
    List<Despesa> buscarPorDescricaoContaCartaoData(
            @Param("descricao") String descricao,
            @Param("contaId") Long contaId,
            @Param("cartaoId") Long cartaoId,
            @Param("data") LocalDate data);

    // Query para buscar despesas por cartão e período da fatura
    @Query("SELECT d FROM Despesa d " +
           "LEFT JOIN FETCH d.conta " +
           "LEFT JOIN FETCH d.categoria " +
           "LEFT JOIN FETCH d.cartao " +
           "WHERE d.cartao.id = :cartaoId " +
           "AND d.data > :dataInicio " +
           "AND d.data <= :dataFim " +
           "ORDER BY d.data")
    List<Despesa> buscarDespesasPorCartaoEPeriodoFatura(
            @Param("cartaoId") Long cartaoId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    @Query("SELECT d FROM Despesa d " +
           "LEFT JOIN FETCH d.conta " +
           "LEFT JOIN FETCH d.categoria " +
           "LEFT JOIN FETCH d.cartao " +
           "WHERE LOWER(d.descricao) LIKE LOWER(CONCAT('%', :descricao, '%')) " +
           "AND (:mes IS NULL OR MONTH(d.data) = :mes) " +
           "AND (:ano IS NULL OR YEAR(d.data) = :ano)")
    List<Despesa> findByDescricaoAndMesAnoSemPaginar(
            @Param("descricao") String descricao,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);

    @Query("SELECT d FROM Despesa d " +
           "LEFT JOIN d.conta " +
           "LEFT JOIN d.categoria " +
           "LEFT JOIN d.cartao " +
           "WHERE LOWER(d.descricao) LIKE LOWER(CONCAT('%', :descricao, '%')) " +
           "AND (:mes IS NULL OR MONTH(d.data) = :mes) " +
           "AND (:ano IS NULL OR YEAR(d.data) = :ano)")
    Page<Despesa> findByDescricaoAndMesAno(
            @Param("descricao") String descricao,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            Pageable pageable);

    @Query("SELECT d FROM Despesa d " +
           "LEFT JOIN FETCH d.conta " +
           "LEFT JOIN FETCH d.categoria " +
           "LEFT JOIN FETCH d.cartao " +
           "WHERE (:descricao IS NULL OR LOWER(d.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))) " +
           "AND (:mes IS NULL OR MONTH(d.data) = :mes) " +
           "AND (:ano IS NULL OR YEAR(d.data) = :ano)")
    List<Despesa> findByFiltrosSemPaginar(
            @Param("descricao") String descricao,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);

    @Query(value = "SELECT d FROM Despesa d " +
           "LEFT JOIN d.conta " +
           "LEFT JOIN d.categoria " +
           "LEFT JOIN d.cartao " +
           "WHERE (:descricao IS NULL OR LOWER(d.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))) " +
           "AND (:mes IS NULL OR MONTH(d.data) = :mes) " +
           "AND (:ano IS NULL OR YEAR(d.data) = :ano) " +
           "ORDER BY d.data DESC")
    Page<Despesa> findByFiltros(
            @Param("descricao") String descricao,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.pago = false")
    Double buscarTotalDespesas();

    Optional<Despesa> findByIdAndPagoTrue(Long id);
} 