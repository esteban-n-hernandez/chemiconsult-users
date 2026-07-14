package com.chemiconsult.repository;

import com.chemiconsult.entity.StockItemDE;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItemDE, Long> {
}
