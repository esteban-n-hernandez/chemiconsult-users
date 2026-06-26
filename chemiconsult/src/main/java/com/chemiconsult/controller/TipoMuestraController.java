package com.chemiconsult.controller;

import com.chemiconsult.entity.TipoMuestraDE;
import com.chemiconsult.service.TipoMuestraService;
import com.chemiconsult.to.TipoMuestraTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tipos-muestra")
@CrossOrigin(origins = "*")
public class TipoMuestraController {

    @Autowired
    private TipoMuestraService tipoMuestraService;

    // GET /api/tipos-muestra — solo activos
    @GetMapping
    public List<TipoMuestraDE> getTiposMuestra() {
        return tipoMuestraService.getTiposMuestra();
    }

    // GET /api/tipos-muestra/todos — todos
    @GetMapping("/todos")
    public List<TipoMuestraDE> getTiposMuestraTodos() {
        return tipoMuestraService.getTiposMuestraTodos();
    }

    // GET /api/tipos-muestra/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TipoMuestraDE> getTipoMuestra(@PathVariable Long id) {
        Optional<TipoMuestraDE> tipoMuestra = tipoMuestraService.getTipoMuestra(id);
        return tipoMuestra
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/tipos-muestra
    @PostMapping
    public ResponseEntity<TipoMuestraDE> createTipoMuestra(@RequestBody TipoMuestraTO to) {
        TipoMuestraDE creado = tipoMuestraService.createTipoMuestra(to);
        return ResponseEntity.status(201).body(creado);
    }

    // PUT /api/tipos-muestra/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TipoMuestraDE> updateTipoMuestra(
            @PathVariable Long id,
            @RequestBody TipoMuestraTO to) {
        TipoMuestraDE actualizado = tipoMuestraService.updateTipoMuestra(id, to);
        return ResponseEntity.ok(actualizado);
    }

    // PATCH /api/tipos-muestra/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarTipoMuestra(@PathVariable Long id) {
        tipoMuestraService.desactivarTipoMuestra(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/tipos-muestra/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTipoMuestra(@PathVariable Long id) {
        tipoMuestraService.deleteTipoMuestra(id);
        return ResponseEntity.noContent().build();
    }
}
