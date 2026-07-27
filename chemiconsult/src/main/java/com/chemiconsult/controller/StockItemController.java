package com.chemiconsult.controller;

import com.chemiconsult.service.StockItemService;
import com.chemiconsult.to.StockItemTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/stock")
public class StockItemController {

    private final StockItemService stockItemService;

    @Autowired
    public StockItemController(StockItemService stockItemService) {
        this.stockItemService = stockItemService;
    }

    // GET /api/stock
    @GetMapping
    public ResponseEntity<List<StockItemTO>> getAll() {
        log.info("Obteniendo todos los Ã­tems de stock");
        return ResponseEntity.ok(stockItemService.getAll());
    }

    // POST /api/stock
    @PostMapping
    public ResponseEntity<StockItemTO> create(@RequestBody StockItemTO to) {
        log.info("Creando Ã­tem de stock: {}", to.getNombre());
        return ResponseEntity.status(201).body(stockItemService.create(to));
    }

    // PUT /api/stock/{id}
    @PutMapping("/{id}")
    public ResponseEntity<StockItemTO> update(@PathVariable Long id, @RequestBody StockItemTO to) {
        log.info("Actualizando Ã­tem de stock con ID: {}", id);
        return ResponseEntity.ok(stockItemService.update(id, to));
    }

    // DELETE /api/stock/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Eliminando Ã­tem de stock con ID: {}", id);
        stockItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
