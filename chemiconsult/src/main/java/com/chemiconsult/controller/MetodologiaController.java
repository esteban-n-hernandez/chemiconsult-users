package com.chemiconsult.controller;

import com.chemiconsult.entity.MetodologiaDE;
import com.chemiconsult.service.MetodologiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodologias")
public class MetodologiaController {

    @Autowired
    private MetodologiaService service;

    @GetMapping
    public ResponseEntity<List<MetodologiaDE>> obtenerTodas() {
        return ResponseEntity.ok(service.obtenerActivas());
    }

    @PostMapping
    public ResponseEntity<MetodologiaDE> crear(@RequestBody MetodologiaDE metodologia) {
        MetodologiaDE nuevaMetodologia = service.guardar(metodologia);
        return ResponseEntity.ok(nuevaMetodologia);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.bajaLogica(id);
        // Retornamos 204 No Content indicando que la operaciÃ³n fue exitosa
        return ResponseEntity.noContent().build();
    }
}