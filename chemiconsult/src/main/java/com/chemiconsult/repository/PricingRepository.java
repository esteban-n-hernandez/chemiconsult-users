package com.chemiconsult.repository;

import com.chemiconsult.entity.PreciosDE;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingRepository extends JpaRepository<PreciosDE, Long> {
}
