package com.chemiconsult.controller;

import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.service.ParametroService;
import com.chemiconsult.to.ParametroTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/parametros")
@CrossOrigin(origins = "*")
public class ParametroController {

    @Autowired
    private ParametroService parametroService;

    // GET /api/parametros — solo activos
    @GetMapping
    public List<ParametroDE> getParametros() {
        return parametroService.getParametros();
    }

    // GET /api/parametros/todos — todos incluyendo inactivos
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

    // PATCH /api/parametros/{id}/desactivar — baja lógica
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
}
