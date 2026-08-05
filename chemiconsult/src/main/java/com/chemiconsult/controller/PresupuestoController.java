package com.chemiconsult.controller;

import com.chemiconsult.service.PresupuestoService;
import com.chemiconsult.to.PresupuestoTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presupuesto")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    @Autowired
    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody PresupuestoTO presupuesto) {
        byte[] pdf = presupuestoService.generatePresupuesto(presupuesto);

        String nro = presupuesto.getNumeroPresupuesto() != null
                ? String.format("%07d", presupuesto.getNumeroPresupuesto())
                : "nuevo";
        String filename = "presupuesto-" + nro + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
