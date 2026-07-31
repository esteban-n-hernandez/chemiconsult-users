package com.chemiconsult.controller;

import com.chemiconsult.service.NumeradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        long siguiente = numeradorService.getPreview(nombre);
        return ResponseEntity.ok(Map.of("siguiente", siguiente));
    }
}
