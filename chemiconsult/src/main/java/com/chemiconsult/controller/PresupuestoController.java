package com.chemiconsult.controller;

import com.chemiconsult.service.PresupuestoService;
import com.chemiconsult.to.CambiarEstadoTO;
import com.chemiconsult.to.PresupuestoResumenTO;
import com.chemiconsult.to.PresupuestoTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuesto")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    @PostMapping("/generar")
    public ResponseEntity<Map<String, Long>> generar(@RequestBody PresupuestoTO presupuesto) {
        PresupuestoService.CrearResult result = presupuestoService.generatePresupuesto(presupuesto);
        return ResponseEntity.ok(Map.of("id", result.id(), "numero", result.numero()));
    }

    @GetMapping
    public List<PresupuestoResumenTO> listar() {
        return presupuestoService.listar();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        PresupuestoService.PdfPresupuesto result = presupuestoService.getPdf(id);
        String nro = String.format("%07d", result.numero());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Presupuesto-" + nro + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(result.pdf());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoTO req) {
        presupuestoService.cambiarEstado(id, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        presupuestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
