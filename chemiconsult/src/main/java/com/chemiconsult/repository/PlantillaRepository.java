package com.chemiconsult.repository;

import com.chemiconsult.entity.PlantillaDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaRepository extends JpaRepository<PlantillaDE, Long> {
    List<PlantillaDE> findByActivoTrue();
}
