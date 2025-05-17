package com.marciliojr.pirangueiro.repository;

import com.marciliojr.pirangueiro.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByNomeContainingIgnoreCase(String nome);
    List<Categoria> findByTipoReceita(Boolean tipoReceita);
} 