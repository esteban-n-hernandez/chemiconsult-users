package com.chemiconsult.controller;

import com.chemiconsult.service.FacturaService;
import com.chemiconsult.to.FacturaResumenTO;
import com.chemiconsult.to.FacturaSolicitudTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/adjuntar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> adjuntar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam String clienteNombre,
            @RequestParam(required = false) String clienteCuit,
            @RequestParam(required = false) String clienteDireccion,
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String fechaEmision,
            @RequestParam(required = false) Long numero,
            @RequestParam(required = false) Integer puntoVenta,
            @RequestParam(required = false) Double total,
            @RequestParam(required = false) MultipartFile archivo) {
        long id = facturaService.adjuntar(clienteId, clienteNombre, clienteCuit, clienteDireccion,
                condicionIva, tipo, fechaEmision, numero, puntoVenta, total, archivo);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/cliente/{clienteId}")
    public List<FacturaResumenTO> listarPorCliente(@PathVariable Long clienteId) {
        return facturaService.listarPorCliente(clienteId);
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> getArchivo(@PathVariable Long id) {
        FacturaService.ArchivoFactura a = facturaService.getArchivo(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + a.nombre() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(a.datos());
    }
}
