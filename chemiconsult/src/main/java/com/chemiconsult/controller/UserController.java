package com.chemiconsult.controller;

import com.chemiconsult.enums.ModuloEnum;
import com.chemiconsult.service.UserService;
import com.chemiconsult.to.CambiarPasswordTO;
import com.chemiconsult.to.UserCreateTO;
import com.chemiconsult.to.UserPerfilTO;
import com.chemiconsult.to.UserTO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserTO> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserTOById(id));
    }

    @PostMapping
    public ResponseEntity<UserTO> createEmpleado(@RequestBody UserCreateTO to) {
        return ResponseEntity.status(201).body(userService.createEmpleado(to));
    }

    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("passwordNueva"));
        return ResponseEntity.noContent().build();
    }

    // Edita username/email â€” NO toca password (ver /password abajo)
    @PutMapping("/{id}")
    public ResponseEntity<UserTO> updateUser(@PathVariable Long id, @RequestBody UserPerfilTO to) {
        return ResponseEntity.ok(userService.updatePerfil(id, to));
    }

    // Cambio de contraseÃ±a â€” requiere la contraseÃ±a actual
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id, @RequestBody CambiarPasswordTO to) {
        userService.cambiarPassword(id, to);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/asignables")
    public List<UserTO> getUsersAsignables() {
        return userService.getUsersAsignables();
    }

    @GetMapping("/me/modulos")
    public ResponseEntity<Set<ModuloEnum>> getMisModulos(Authentication auth) {
        return ResponseEntity.ok(userService.getModulosForUser(auth.getName()));
    }

    @PutMapping("/{id}/modulos")
    public ResponseEntity<Set<ModuloEnum>> setModulos(@PathVariable Long id,
                                                       @RequestBody Set<ModuloEnum> modulos) {
        return ResponseEntity.ok(userService.setModulos(id, modulos));
    }

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
}