package com.chemiconsult.controller;

import com.chemiconsult.entity.CategoriaDocumentoDE;
import com.chemiconsult.service.CategoriaDocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias-documento")
public class CategoriaDocumentoController {

    private final CategoriaDocumentoService service;

    @Autowired
    public CategoriaDocumentoController(CategoriaDocumentoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDocumentoDE>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<CategoriaDocumentoDE> create(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(201).body(service.create(nombre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDocumentoDE> actualizar(@PathVariable Long id,
                                                            @RequestBody Map<String, String> body) {
        String nombre = body.getOrDefault("nombre", "").trim();
        if (nombre.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(service.actualizar(id, nombre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
