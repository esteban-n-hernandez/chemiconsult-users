package com.chemiconsult.controller;

import com.chemiconsult.service.ColaAnalisisService;
import com.chemiconsult.to.ColaAnalisisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PatchMapping("/{id}/analizado")
    public ResponseEntity<Void> toggleAnalizado(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        colaAnalisisService.toggleAnalizado(id, username);
        return ResponseEntity.ok().build();
    }
}
