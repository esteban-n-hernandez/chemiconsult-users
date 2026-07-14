package com.chemiconsult.service;

import com.chemiconsult.entity.StockItemDE;
import com.chemiconsult.mapper.StockItemMapper;
import com.chemiconsult.repository.StockItemRepository;
import com.chemiconsult.to.StockItemTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockItemService {

    private final StockItemRepository stockItemRepository;

    @Autowired
    public StockItemService(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    public List<StockItemTO> getAll() {
        return StockItemMapper.mapToTO(stockItemRepository.findAll());
    }

    public StockItemTO create(StockItemTO to) {
        StockItemDE entity = StockItemMapper.mapToEntity(to);
        return StockItemMapper.mapToTO(stockItemRepository.save(entity));
    }

    public StockItemTO update(Long id, StockItemTO to) {
        StockItemDE entity = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ítem de stock no encontrado con ID: " + id));
        StockItemMapper.applyUpdate(entity, to);
        return StockItemMapper.mapToTO(stockItemRepository.save(entity));
    }

    public void delete(Long id) {
        if (!stockItemRepository.existsById(id)) {
            throw new RuntimeException("Ítem de stock no encontrado con ID: " + id);
        }
        stockItemRepository.deleteById(id);
    }
}
