package com.chemiconsult.repository;

import com.chemiconsult.entity.DocumentoDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<DocumentoDE, Long> {
    List<DocumentoDE> findAllByOrderByCreatedDateDesc();
}
