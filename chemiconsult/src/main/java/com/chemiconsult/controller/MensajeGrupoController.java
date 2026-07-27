package com.chemiconsult.controller;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.service.MensajeGrupoService;
import com.chemiconsult.to.MensajeGrupoTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes/grupo")
public class MensajeGrupoController {

    private final MensajeGrupoService mensajeGrupoService;
    private final UserRepository userRepository;

    @Autowired
    public MensajeGrupoController(MensajeGrupoService mensajeGrupoService, UserRepository userRepository) {
        this.mensajeGrupoService = mensajeGrupoService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<MensajeGrupoTO>> getMensajes(
            @RequestParam(defaultValue = "10") int limite,
            @RequestParam(required = false) Long antes,
            @RequestParam(required = false) Long despues) {
        if (antes != null) return ResponseEntity.ok(mensajeGrupoService.getAntesDe(antes, limite));
        if (despues != null) return ResponseEntity.ok(mensajeGrupoService.getDespuesDe(despues));
        return ResponseEntity.ok(mensajeGrupoService.getUltimos(limite));
    }

    @PostMapping
    public ResponseEntity<MensajeGrupoTO> enviar(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails principal) {
        Long miId = resolverUserId(principal);
        String contenido = body.get("contenido").toString();
        if (contenido.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.status(201).body(mensajeGrupoService.enviar(miId, contenido));
    }

    private Long resolverUserId(UserDetails principal) {
        UserDE user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + principal.getUsername()));
        return user.getId();
    }
}
