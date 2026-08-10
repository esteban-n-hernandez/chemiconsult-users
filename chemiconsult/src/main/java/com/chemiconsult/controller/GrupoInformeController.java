package com.chemiconsult.controller;

import com.chemiconsult.entity.GrupoInformeDE;
import com.chemiconsult.service.GrupoInformeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos-informe")
public class GrupoInformeController {

    private final GrupoInformeService service;

    public GrupoInformeController(GrupoInformeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<GrupoInformeDE>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<GrupoInformeDE> crear(@RequestBody Map<String, Object> body) {
        String codigo = (String) body.get("codigo");
        String label  = (String) body.get("label");
        Integer orden = body.get("orden") != null ? ((Number) body.get("orden")).intValue() : 0;
        return ResponseEntity.status(201).body(service.crear(codigo, label, orden));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GrupoInformeDE> actualizar(@PathVariable Long id,
                                                      @RequestBody Map<String, Object> body) {
        String codigo = (String) body.get("codigo");
        String label  = (String) body.get("label");
        Integer orden = body.get("orden") != null ? ((Number) body.get("orden")).intValue() : null;
        return ResponseEntity.ok(service.actualizar(id, codigo, label, orden));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
