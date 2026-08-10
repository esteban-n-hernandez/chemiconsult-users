package com.chemiconsult.controller;

import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.entity.ParametroMetodologiaDE;
import com.chemiconsult.service.ParametroService;
import com.chemiconsult.to.ParametroTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/parametros")
public class ParametroController {

    ParametroService parametroService;

    // GET /api/parametros â€" solo activos
    @GetMapping
    public List<ParametroDE> getParametros() {
        return parametroService.getParametros();
    }

    // GET /api/parametros/todos â€" todos incluyendo inactivos
    @GetMapping("/todos")
    public List<ParametroDE> getParametrosTodos() {
        return parametroService.getParametrosTodos();
    }

    // GET /api/parametros/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ParametroDE> getParametro(@PathVariable Long id) {
        Optional<ParametroDE> parametro = parametroService.getParametro(id);
        return parametro
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/parametros
    @PostMapping
    public ResponseEntity<ParametroDE> createParametro(@RequestBody ParametroTO to) {
        ParametroDE creado = parametroService.createParametro(to);
        return ResponseEntity.status(201).body(creado);
    }

    // PUT /api/parametros/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ParametroDE> updateParametro(
            @PathVariable Long id,
            @RequestBody ParametroTO to) {
        ParametroDE actualizado = parametroService.updateParametro(id, to);
        return ResponseEntity.ok(actualizado);
    }

    // PATCH /api/parametros/{id}/desactivar â€" baja lÃ³gica
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarParametro(@PathVariable Long id) {
        parametroService.desactivarParametro(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/parametros/{id} — baja física
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParametro(@PathVariable Long id) {
        parametroService.deleteParametro(id);
        return ResponseEntity.noContent().build();
    }

    // ── Metodologías ──────────────────────────────────────────────────────────

    // GET /api/parametros/{id}/metodologias?matrizId={matrizId}
    @GetMapping("/{id}/metodologias")
    public ResponseEntity<List<ParametroMetodologiaDE>> getMetodologias(
            @PathVariable Long id,
            @RequestParam(required = false) Long matrizId) {
        return ResponseEntity.ok(parametroService.getMetodologias(id, matrizId));
    }

    // POST /api/parametros/{id}/metodologias  body: { metodologiaId, matrizId?, tipoAnalisis? }
    @PostMapping("/{id}/metodologias")
    public ResponseEntity<ParametroMetodologiaDE> addMetodologia(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long metodologiaId = body.get("metodologiaId") != null ? ((Number) body.get("metodologiaId")).longValue() : null;
        Long matrizId      = body.get("matrizId")      != null ? ((Number) body.get("matrizId")).longValue()      : null;
        String tipoAnalisis = body.get("tipoAnalisis") != null ? body.get("tipoAnalisis").toString() : null;
        ParametroMetodologiaDE pm = parametroService.addMetodologia(id, metodologiaId, matrizId, tipoAnalisis);
        return ResponseEntity.status(201).body(pm);
    }

    // PATCH /api/parametros/{id}/metodologias/{pmId}  body: { tipoAnalisis }
    @PatchMapping("/{id}/metodologias/{pmId}")
    public ResponseEntity<ParametroMetodologiaDE> updateMetodologiaTipo(
            @PathVariable Long id,
            @PathVariable Long pmId,
            @RequestBody Map<String, String> body) {
        ParametroMetodologiaDE pm = parametroService.updateMetodologiaTipo(id, pmId, body.get("tipoAnalisis"));
        return ResponseEntity.ok(pm);
    }

    // DELETE /api/parametros/{id}/metodologias/{pmId}
    @DeleteMapping("/{id}/metodologias/{pmId}")
    public ResponseEntity<Void> removeMetodologia(@PathVariable Long id, @PathVariable Long pmId) {
        parametroService.removeMetodologia(id, pmId);
        return ResponseEntity.noContent().build();
    }

    @Autowired
    public ParametroController(ParametroService parametroService) {
        this.parametroService = parametroService;
    }
}
