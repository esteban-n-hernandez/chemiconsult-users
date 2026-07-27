package com.chemiconsult.controller;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.service.MensajeService;
import com.chemiconsult.to.ConversacionTO;
import com.chemiconsult.to.MensajeTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;
    private final UserRepository userRepository;

    @Autowired
    public MensajeController(MensajeService mensajeService, UserRepository userRepository) {
        this.mensajeService = mensajeService;
        this.userRepository = userRepository;
    }

    @GetMapping("/conversaciones")
    public ResponseEntity<List<ConversacionTO>> getConversaciones(@AuthenticationPrincipal UserDetails principal) {
        Long miId = resolverUserId(principal);
        return ResponseEntity.ok(mensajeService.getConversaciones(miId));
    }

    @GetMapping("/conversacion/{otroId}")
    public ResponseEntity<List<MensajeTO>> getConversacion(
            @PathVariable Long otroId,
            @RequestParam(defaultValue = "10") int limite,
            @RequestParam(required = false) Long antes,
            @RequestParam(required = false) Long despues,
            @AuthenticationPrincipal UserDetails principal) {
        Long miId = resolverUserId(principal);
        if (antes != null) {
            return ResponseEntity.ok(mensajeService.getMensajesAntesDe(miId, otroId, antes, limite));
        }
        if (despues != null) {
            return ResponseEntity.ok(mensajeService.getMensajesDespuesDe(miId, otroId, despues));
        }
        return ResponseEntity.ok(mensajeService.getUltimosMensajes(miId, otroId, limite));
    }

    @PostMapping
    public ResponseEntity<MensajeTO> enviar(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails principal) {
        Long miId = resolverUserId(principal);
        Long receptorId = Long.valueOf(body.get("receptorId").toString());
        String contenido = body.get("contenido").toString();
        if (contenido.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.status(201).body(mensajeService.enviar(miId, receptorId, contenido));
    }

    @PutMapping("/leer/{emisorId}")
    public ResponseEntity<Void> marcarLeidos(
            @PathVariable Long emisorId,
            @AuthenticationPrincipal UserDetails principal) {
        Long miId = resolverUserId(principal);
        mensajeService.marcarLeidos(miId, emisorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/no-leidos")
    public ResponseEntity<Map<String, Long>> getNoLeidos(@AuthenticationPrincipal UserDetails principal) {
        Long miId = resolverUserId(principal);
        return ResponseEntity.ok(Map.of("total", mensajeService.getNoLeidosCount(miId)));
    }

    @GetMapping("/ultimo-leido/{receptorId}")
    public ResponseEntity<Map<String, Long>> getUltimoLeido(
            @PathVariable Long receptorId,
            @AuthenticationPrincipal UserDetails principal) {
        Long miId = resolverUserId(principal);
        return ResponseEntity.ok(Map.of("ultimoLeidoId", mensajeService.getUltimoMensajeLeidoId(miId, receptorId)));
    }

    private Long resolverUserId(UserDetails principal) {
        UserDE user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + principal.getUsername()));
        return user.getId();
    }
}
