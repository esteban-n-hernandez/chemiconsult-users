package com.chemiconsult.controller;

import com.chemiconsult.service.ClienteContactoService;
import com.chemiconsult.to.ClienteContactoTO;
import com.chemiconsult.to.SucursalContactoResumenTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteContactoController {

    private final ClienteContactoService contactoService;

    // GET /api/clientes/{clienteId}/contactos â€” todos los contactos de la empresa
    @GetMapping("/{clienteId}/contactos")
    public ResponseEntity<List<ClienteContactoTO>> getContactos(@PathVariable Long clienteId) {
        return ResponseEntity.ok(contactoService.getContactosPorCliente(clienteId));
    }

    // GET /api/clientes/sucursales/{sucursalId}/contactos â€” quiÃ©nes reciben avisos de esta sucursal
    @GetMapping("/sucursales/{sucursalId}/contactos")
    public ResponseEntity<List<SucursalContactoResumenTO>> getContactosDeSucursal(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(contactoService.getContactosPorSucursal(sucursalId));
    }

    @PostMapping("/contactos")
    public ResponseEntity<ClienteContactoTO> createContacto(@RequestBody ClienteContactoTO to) {
        return ResponseEntity.status(201).body(contactoService.createContacto(to));
    }

    @PutMapping("/contactos/{id}")
    public ResponseEntity<ClienteContactoTO> updateContacto(@PathVariable Long id, @RequestBody ClienteContactoTO to) {
        return ResponseEntity.ok(contactoService.updateContacto(id, to));
    }

    @PatchMapping("/contactos/{id}/desactivar")
    public ResponseEntity<Void> desactivarContacto(@PathVariable Long id) {
        contactoService.desactivarContacto(id);
        return ResponseEntity.noContent().build();
    }

    @Autowired
    public ClienteContactoController(ClienteContactoService contactoService) {
        this.contactoService = contactoService;
    }
}