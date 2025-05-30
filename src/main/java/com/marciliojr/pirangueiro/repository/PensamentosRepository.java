package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Pensamentos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PensamentosRepository extends JpaRepository<Pensamentos, Long> {
    
    @Query(value = "SELECT * FROM pensamentos ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Pensamentos> findRandomPensamento();
} 