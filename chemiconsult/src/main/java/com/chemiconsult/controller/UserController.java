package com.chemiconsult.controller;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.service.UserService;
import com.chemiconsult.to.CambiarPasswordTO;
import com.chemiconsult.to.UserCreateTO;
import com.chemiconsult.to.UserPerfilTO;
import com.chemiconsult.to.UserTO;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
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

    // Edita username/email — NO toca password (ver /password abajo)
    @PutMapping("/{id}")
    public ResponseEntity<UserTO> updateUser(@PathVariable Long id, @RequestBody UserPerfilTO to) {
        return ResponseEntity.ok(userService.updatePerfil(id, to));
    }

    // Cambio de contraseña — requiere la contraseña actual
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id, @RequestBody CambiarPasswordTO to) {
        userService.cambiarPassword(id, to);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // Agregar a UserController:

    @GetMapping("/asignables")
    public List<UserTO> getUsersAsignables() {
        return userService.getUsersAsignables();
    }

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
}