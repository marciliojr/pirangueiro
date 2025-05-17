package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByLidaFalseOrderByDataGeracaoDesc();
} 