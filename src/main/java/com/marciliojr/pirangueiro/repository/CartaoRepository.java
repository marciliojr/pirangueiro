package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CartaoRepository extends JpaRepository<Cartao, Long> {
    List<Cartao> findByNomeContainingIgnoreCase(String nome);
    
    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.cartao.id = :cartaoId AND d.pago = false")
    Double calcularTotalDespesasPorCartao(@Param("cartaoId") Long cartaoId);

    @Query("SELECT COUNT(d) > 0 FROM Despesa d WHERE d.cartao.id = :cartaoId AND d.pago = false")
    boolean existeDespesasPorCartao(@Param("cartaoId") Long cartaoId);

} 