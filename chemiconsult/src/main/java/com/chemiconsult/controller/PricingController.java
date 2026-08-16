package com.chemiconsult.controller;

import com.chemiconsult.service.PricingService;
import com.chemiconsult.to.PreciosTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/precios")
@Log4j2
public class PricingController {

    PricingService pricingService;

    @GetMapping
    public List<PreciosTO> getPrecios() {
        return pricingService.getPricing();
    }

    @PostMapping
    public ResponseEntity<PreciosTO> addPrecio(@RequestBody PreciosTO preciosTO) {
        log.info("Adding new pricing information: {}", preciosTO);
        return ResponseEntity.status(201).body(pricingService.addPrecio(preciosTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreciosTO> updatePrecio(@PathVariable Long id, @RequestBody PreciosTO preciosTO) {
        log.info("Updating pricing id={}: {}", id, preciosTO);
        return ResponseEntity.ok(pricingService.updatePrecio(id, preciosTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrecio(@PathVariable Long id) {
        log.info("Deleting pricing id={}", id);
        pricingService.deletePrecio(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ajuste")
    public ResponseEntity<List<PreciosTO>> aplicarFactor(@RequestBody Map<String, Object> body) {
        Double factor = ((Number) body.get("factor")).doubleValue();
        if (factor <= 0) return ResponseEntity.badRequest().build();
        List<Long> ids = extractIds(body);
        log.info("Applying pricing factor={} to ids={}", factor, ids);
        return ResponseEntity.ok(pricingService.aplicarFactor(factor, ids));
    }

    @PostMapping("/recalcular-impuesto")
    public ResponseEntity<List<PreciosTO>> recalcularImpuesto(@RequestBody Map<String, Object> body) {
        Double porcentaje = ((Number) body.get("porcentaje")).doubleValue();
        if (porcentaje < 0) return ResponseEntity.badRequest().build();
        List<Long> ids = extractIds(body);
        log.info("Recalculating tax {}% for ids={}", porcentaje, ids);
        return ResponseEntity.ok(pricingService.recalcularImpuesto(porcentaje, ids));
    }

    @SuppressWarnings("unchecked")
    private List<Long> extractIds(Map<String, Object> body) {
        Object raw = body.get("ids");
        if (raw == null) return null;
        return ((List<Number>) raw).stream().map(Number::longValue).collect(Collectors.toList());
    }

    @Autowired
    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }
}
