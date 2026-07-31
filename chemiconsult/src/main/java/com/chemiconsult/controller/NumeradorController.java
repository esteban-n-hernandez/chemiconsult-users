package com.chemiconsult.controller;

import com.chemiconsult.entity.NumeradorDE;
import com.chemiconsult.service.NumeradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/numeradores")
public class NumeradorController {

    private final NumeradorService numeradorService;

    public NumeradorController(NumeradorService numeradorService) {
        this.numeradorService = numeradorService;
    }

    @GetMapping("/preview/{nombre}")
    public ResponseEntity<Map<String, Long>> preview(@PathVariable String nombre) {
        return ResponseEntity.ok(Map.of("siguiente", numeradorService.getPreview(nombre)));
    }

    @GetMapping
    public List<NumeradorDE> listar() {
        return numeradorService.listar();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        numeradorService.actualizar(id, body.get("valor"));
        return ResponseEntity.noContent().build();
    }
}
