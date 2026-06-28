package com.chemiconsult.controller;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.mapper.UserMapper;
import com.chemiconsult.service.ClienteService;
import com.chemiconsult.service.UserService;
import com.chemiconsult.to.AsignarUsuarioTO;
import com.chemiconsult.to.ClienteTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UserService userService;

    // GET /api/clientes — solo activos
    @GetMapping
    public List<ClienteDE> getClientes() {
        log.info("Obteniendo clientes activos");
        return clienteService.getClientes();
    }

    // GET /api/clientes/todos — todos
    @GetMapping("/todos")
    public List<ClienteDE> getClientesTodos() {
        log.info("Obteniendo todos los clientes");
        return clienteService.getClientesTodos();
    }

    // GET /api/clientes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDE> getCliente(@PathVariable Long id) {
        log.info("Obteniendo cliente con ID: {}", id);
        return ResponseEntity.ok(clienteService.getCliente(id));
    }

    // POST /api/clientes
    @PostMapping
    public ResponseEntity<ClienteDE> createCliente(@RequestBody ClienteTO to) {
        log.info("Creando nuevo cliente: {}", to);
        ClienteDE creado = clienteService.createCliente(to);
        UserDE user = UserMapper.createUserFromCliente(creado);

        log.info("Creando usuario asociado al cliente: {}", user);
        userService.createUser(user);
        return ResponseEntity.status(201).body(creado);
    }

    // PUT /api/clientes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDE> updateCliente(
            @PathVariable Long id,
            @RequestBody ClienteTO to) {
        log.info("Actualizando cliente con ID: {} con datos: {}", id, to);
        ClienteDE actualizado = clienteService.updateCliente(id, to);
        return ResponseEntity.ok(actualizado);
    }

    // PATCH /api/clientes/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarCliente(@PathVariable Long id) {
        log.info("Desactivando cliente con ID: {}", id);
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/clientes/{id}/asignar-usuario
    @PatchMapping("/{id}/asignar-usuario")
    public ResponseEntity<ClienteDE> asignarUsuario(
            @PathVariable Long id,
            @RequestBody AsignarUsuarioTO to) {
        log.info("Asignando usuario con ID: {} al cliente con ID: {}", to.getUsername(), id);
        ClienteDE actualizado = clienteService.asignarUsuario(id, to);
        return ResponseEntity.ok(actualizado);
    }

    // DELETE /api/clientes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        log.info("Eliminando cliente con ID: {}", id);
        clienteService.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }
}
