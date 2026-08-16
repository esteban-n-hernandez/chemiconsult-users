package com.chemiconsult.controller;

import com.chemiconsult.service.FacturaService;
import com.chemiconsult.to.FacturaResumenTO;
import com.chemiconsult.to.FacturaSolicitudTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factura")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @PostMapping("/emitir")
    public ResponseEntity<Map<String, Object>> emitir(@RequestBody FacturaSolicitudTO req) {
        FacturaService.EmitirResult r = facturaService.emitir(req);
        return ResponseEntity.ok(Map.of(
                "id",                 r.id(),
                "numero",             r.numero(),
                "cae",                r.cae() != null ? r.cae() : "",
                "caeFechaVencimiento",r.caeFechaVencimiento() != null ? r.caeFechaVencimiento().toString() : "",
                "autorizada",         r.autorizada()
        ));
    }

    @GetMapping
    public List<FacturaResumenTO> listar() {
        return facturaService.listar();
    }

    @GetMapping("/{id}")
    public FacturaResumenTO getDetalle(@PathVariable Long id) {
        return facturaService.getDetalle(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        FacturaService.PdfFactura r = facturaService.getPdf(id);
        String nombre = String.format("Factura%s-%04d-%08d.pdf",
                r.tipo().name(), r.puntoVenta(), r.numero());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(r.pdf());
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        facturaService.anular(id);
        return ResponseEntity.ok().build();
    }
}
