package com.chemiconsult.controller;

import com.chemiconsult.entity.ResolucionDE;
import com.chemiconsult.service.ResolucionService;
import com.chemiconsult.to.MatrizResolucionesTO;
import com.chemiconsult.to.ParametroNormaTO;
import com.chemiconsult.to.ResolucionDestinoTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resoluciones")
public class ResolucionController {

    private final ResolucionService resolucionService;

    public ResolucionController(ResolucionService resolucionService) {
        this.resolucionService = resolucionService;
    }

    @GetMapping
    public ResponseEntity<List<ResolucionDE>> getAll() {
        return ResponseEntity.ok(resolucionService.getAll());
    }

    // Ãrbol completo: Matriz â†’ Resoluciones â†’ Destinos â†’ ParÃ¡metros
    @GetMapping("/por-matriz/{matrizId}")
    public ResponseEntity<MatrizResolucionesTO> getArbolPorMatriz(@PathVariable Long matrizId) {
        return ResponseEntity.ok(resolucionService.obtenerArbolPorMatriz(matrizId));
    }

    @GetMapping("/{id}/parametros")
    public ResponseEntity<List<ParametroNormaTO>> getParametros(@PathVariable Long id) {
        return ResponseEntity.ok(resolucionService.obtenerParametrosPorDestino(id));
    }
}