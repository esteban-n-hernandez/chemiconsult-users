package com.chemiconsult.controller;

import com.chemiconsult.entity.EquipoDE;
import com.chemiconsult.service.EquipoService;
import com.chemiconsult.to.EquipoTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService service;

    public EquipoController(EquipoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<EquipoDE>> getActivos() {
        return ResponseEntity.ok(service.getActivos());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<EquipoDE>> getTodos() {
        return ResponseEntity.ok(service.getTodos());
    }

    @PostMapping
    public ResponseEntity<EquipoDE> crear(@RequestBody EquipoTO to) {
        return ResponseEntity.status(201).body(service.crear(to));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoDE> actualizar(@PathVariable Long id, @RequestBody EquipoTO to) {
        return ResponseEntity.ok(service.actualizar(id, to));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
