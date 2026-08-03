package com.chemiconsult.controller;

import com.chemiconsult.service.DocumentoService;
import com.chemiconsult.to.DocumentoTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    @Autowired
    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentoTO>> getAll() {
        return ResponseEntity.ok(documentoService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.getById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoTO> create(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("categoria") String categoria,
            @RequestParam(value = "fechaVencimiento", required = false) String fechaVencimiento,
            @RequestParam("file") MultipartFile file) {
        log.info("Subiendo documento: {}", nombre);
        return ResponseEntity.status(201).body(
                documentoService.create(nombre, descripcion, categoria, fechaVencimiento, file));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentoTO> update(@PathVariable Long id, @RequestBody DocumentoTO to) {
        log.info("Actualizando metadatos del documento ID: {}", id);
        return ResponseEntity.ok(documentoService.updateMetadata(
                id, to.getNombre(), to.getDescripcion(), to.getCategoria(), to.getFechaVencimiento()));
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        DocumentoTO doc = documentoService.getById(id);
        byte[] bytes = documentoService.descargarArchivo(id);
        String filename = doc.getNombreArchivo() != null ? doc.getNombreArchivo() : "documento.pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Eliminando documento ID: {}", id);
        documentoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
