package com.chemiconsult.controller;

import com.chemiconsult.entity.ResolucionDE;
import com.chemiconsult.service.ResolucionService;
import com.chemiconsult.to.MatrizResolucionesTO;
import com.chemiconsult.to.ParametroNormaTO;
import com.chemiconsult.to.ResolucionCreateTO;
import com.chemiconsult.to.ResolucionDestinoTO;
import com.chemiconsult.to.ResolucionTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping
    public ResponseEntity<ResolucionDE> crear(@RequestBody ResolucionCreateTO to) {
        return ResponseEntity.status(201).body(resolucionService.crear(to));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResolucionDE> actualizar(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body) {
        String nombre = body.getOrDefault("nombre", "").trim();
        String descripcion = body.get("descripcion");
        return ResponseEntity.ok(resolucionService.actualizar(id, nombre, descripcion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        resolucionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Ãrbol completo: Matriz â†’ Resoluciones â†’ Destinos â†’ ParÃ¡metros
    @GetMapping("/por-matriz/{matrizId}")
    public ResponseEntity<MatrizResolucionesTO> getArbolPorMatriz(@PathVariable Long matrizId) {
        return ResponseEntity.ok(resolucionService.obtenerArbolPorMatriz(matrizId));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<ResolucionTO> getDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(resolucionService.obtenerDetalle(id));
    }

    @PostMapping("/{id}/parametros/{parametroId}")
    public ResponseEntity<Void> agregarParametro(@PathVariable Long id, @PathVariable Long parametroId,
                                                  @RequestBody(required = false) Map<String, String> body) {
        String unidad = body != null ? body.get("unidad") : null;
        resolucionService.agregarParametro(id, parametroId, unidad);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/parametros/{parametroId}")
    public ResponseEntity<Void> actualizarUnidad(@PathVariable Long id, @PathVariable Long parametroId,
                                                  @RequestBody Map<String, String> body) {
        resolucionService.actualizarUnidad(id, parametroId, body.get("unidad"));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/parametros/{parametroId}/limite")
    public ResponseEntity<Void> actualizarLimite(@PathVariable Long id, @PathVariable Long parametroId,
                                                  @RequestBody Map<String, Object> body) {
        String tipoLimite  = (String) body.get("tipoLimite");
        Double valorMinimo = body.get("valorMinimo") != null ? ((Number) body.get("valorMinimo")).doubleValue() : null;
        Double valorMaximo = body.get("valorMaximo") != null ? ((Number) body.get("valorMaximo")).doubleValue() : null;
        String limiteTexto = (String) body.get("limiteTexto");
        resolucionService.actualizarLimite(id, parametroId, tipoLimite, valorMinimo, valorMaximo, limiteTexto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/parametros/{parametroId}")
    public ResponseEntity<?> quitarParametro(@PathVariable Long id, @PathVariable Long parametroId) {
        try {
            resolucionService.quitarParametro(id, parametroId);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409)
                    .body(Map.of("error", "Este parámetro está referenciado en análisis existentes y no puede eliminarse."));
        }
    }

    @GetMapping("/{id}/parametros")
    public ResponseEntity<List<ParametroNormaTO>> getParametros(@PathVariable Long id) {
        return ResponseEntity.ok(resolucionService.obtenerParametrosPorDestino(id));
    }

    @PostMapping("/{id}/destinos")
    public ResponseEntity<ResolucionDestinoTO> crearDestino(@PathVariable Long id,
                                                             @RequestBody Map<String, String> body) {
        String nombre = body.getOrDefault("nombre", "").trim();
        return ResponseEntity.status(201).body(resolucionService.crearDestino(id, nombre));
    }

    @DeleteMapping("/{id}/destinos/{destinoId}")
    public ResponseEntity<Void> eliminarDestino(@PathVariable Long id, @PathVariable Long destinoId) {
        resolucionService.eliminarDestino(destinoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/destinos/{destinoId}/parametros/{parametroId}")
    public ResponseEntity<Void> agregarParamADestino(@PathVariable Long id,
                                                      @PathVariable Long destinoId,
                                                      @PathVariable Long parametroId,
                                                      @RequestBody(required = false) Map<String, String> body) {
        String unidad = body != null ? body.get("unidad") : null;
        resolucionService.agregarParametroADestino(destinoId, parametroId, unidad);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/destinos/{destinoId}/parametros/{parametroId}/limite")
    public ResponseEntity<Void> actualizarLimiteDestino(@PathVariable Long id,
                                                         @PathVariable Long destinoId,
                                                         @PathVariable Long parametroId,
                                                         @RequestBody Map<String, Object> body) {
        String tipoLimite  = (String) body.get("tipoLimite");
        Double valorMinimo = body.get("valorMinimo") != null ? ((Number) body.get("valorMinimo")).doubleValue() : null;
        Double valorMaximo = body.get("valorMaximo") != null ? ((Number) body.get("valorMaximo")).doubleValue() : null;
        String limiteTexto = (String) body.get("limiteTexto");
        resolucionService.actualizarLimiteDestino(destinoId, parametroId, tipoLimite, valorMinimo, valorMaximo, limiteTexto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/destinos/{destinoId}/parametros/{parametroId}/unidad")
    public ResponseEntity<Void> actualizarUnidadDestino(@PathVariable Long id,
                                                         @PathVariable Long destinoId,
                                                         @PathVariable Long parametroId,
                                                         @RequestBody Map<String, String> body) {
        resolucionService.actualizarUnidadDestino(destinoId, parametroId, body.get("unidad"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/destinos/{destinoId}/parametros/{parametroId}")
    public ResponseEntity<Void> quitarParamDeDestino(@PathVariable Long id,
                                                      @PathVariable Long destinoId,
                                                      @PathVariable Long parametroId) {
        resolucionService.quitarParametroDeDestino(destinoId, parametroId);
        return ResponseEntity.noContent().build();
    }
}