package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    List<Conta> findByNomeContainingIgnoreCase(String nome);
    
    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM Receita r WHERE r.conta.id = :contaId AND MONTH(r.data) = :mes AND YEAR(r.data) = :ano")
    Double calcularTotalReceitasPorContaEMes(@Param("contaId") Long contaId, @Param("mes") int mes, @Param("ano") int ano);
    
    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.conta.id = :contaId AND MONTH(d.data) = :mes AND YEAR(d.data) = :ano")
    Double calcularTotalDespesasPorContaEMes(@Param("contaId") Long contaId, @Param("mes") int mes, @Param("ano") int ano);

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM Receita r WHERE r.conta.id = :contaId")
    Double calcularTotalReceitasPorConta(@Param("contaId") Long contaId);
    
    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.conta.id = :contaId")
    Double calcularTotalDespesasPorConta(@Param("contaId") Long contaId);

    @Query("SELECT COUNT(d) > 0 FROM Despesa d WHERE d.conta.id = :contaId")
    boolean existeDespesaAssociadaConta(@Param("contaId") Long contaId);

    @Query("SELECT COUNT(r) > 0 FROM Receita r WHERE r.conta.id = :contaId")
    boolean existeReceitaAssociadaConta(@Param("contaId") Long contaId);

} 