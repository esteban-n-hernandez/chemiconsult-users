package com.chemiconsult.controller;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.service.UserService;
import com.chemiconsult.to.UserPasswordTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    UserService userService;

    // ✅ Obtener todos los usuarios
    @GetMapping
    public List<UserDE> getUsers() {
        return userService.getUsers();
    }

    // ✅ Obtener usuario por ID
    @GetMapping("/{id}")
    public UserDE getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // ✅ Crear un nuevo usuario
    public UserDE createUser(@RequestBody UserDE user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public UserDE updateUser(@PathVariable Long id, @RequestBody UserDE user) {

        Optional<UserDE> optional = Optional.ofNullable(userService.getUserById(id));
        if (optional.isPresent()) {
            UserDE existing = optional.get();
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setPassword(user.getPassword());
            existing.setRol(user.getRol());

            return userService.createUser(existing);
        }
        throw new RuntimeException("Usuario no encontrado con ID: " + id);
    }

    @PutMapping("/{id}/password")
    public UserDE updateUserPassword(@PathVariable Long id, @RequestBody UserPasswordTO userPasswordTO) {

        Optional<UserDE> optional = Optional.ofNullable(userService.getUserById(id));
        if (optional.isPresent()) {
            UserDE existing = optional.get();
            existing.setPassword(userPasswordTO.getNewPassword());

            return userService.createUser(existing);
        }
        throw new RuntimeException("Usuario no encontrado con ID: " + id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
