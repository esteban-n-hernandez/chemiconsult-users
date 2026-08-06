package com.chemiconsult.controller;

import com.chemiconsult.enums.EstadoAnalisisParametroEnum;
import com.chemiconsult.service.ColaAnalisisService;
import com.chemiconsult.to.ColaAnalisisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mi-cola")
public class ColaAnalisisController {

    @Autowired
    private ColaAnalisisService colaAnalisisService;

    @GetMapping
    public ResponseEntity<List<ColaAnalisisTO>> getMiCola() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(colaAnalisisService.getMiCola(username));
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ColaAnalisisTO>> getTodos() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(colaAnalisisService.getTodos(username));
    }

    @GetMapping("/global")
    public ResponseEntity<List<ColaAnalisisTO>> getTodosGlobal() {
        return ResponseEntity.ok(colaAnalisisService.getTodosGlobal());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        EstadoAnalisisParametroEnum nuevoEstado =
                EstadoAnalisisParametroEnum.valueOf(body.get("estado").toUpperCase());
        colaAnalisisService.cambiarEstado(id, nuevoEstado, username);
        return ResponseEntity.ok().build();
    }
}
