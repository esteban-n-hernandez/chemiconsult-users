package com.chemiconsult.controller;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.service.ClienteService;
import com.chemiconsult.to.AsignarUsuarioTO;
import com.chemiconsult.to.ClienteTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // GET /api/clientes — solo activos
    @GetMapping
    public List<ClienteDE> getClientes() {
        return clienteService.getClientes();
    }

    // GET /api/clientes/todos — todos
    @GetMapping("/todos")
    public List<ClienteDE> getClientesTodos() {
        return clienteService.getClientesTodos();
    }

    // GET /api/clientes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDE> getCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getCliente(id));
    }

    // POST /api/clientes
    @PostMapping
    public ResponseEntity<ClienteDE> createCliente(@RequestBody ClienteTO to) {
        ClienteDE creado = clienteService.createCliente(to);
        return ResponseEntity.status(201).body(creado);
    }

    // PUT /api/clientes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDE> updateCliente(
            @PathVariable Long id,
            @RequestBody ClienteTO to) {
        ClienteDE actualizado = clienteService.updateCliente(id, to);
        return ResponseEntity.ok(actualizado);
    }

    // PATCH /api/clientes/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarCliente(@PathVariable Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/clientes/{id}/asignar-usuario
    @PatchMapping("/{id}/asignar-usuario")
    public ResponseEntity<ClienteDE> asignarUsuario(
            @PathVariable Long id,
            @RequestBody AsignarUsuarioTO to) {
        ClienteDE actualizado = clienteService.asignarUsuario(id, to);
        return ResponseEntity.ok(actualizado);
    }

    // DELETE /api/clientes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }
}
