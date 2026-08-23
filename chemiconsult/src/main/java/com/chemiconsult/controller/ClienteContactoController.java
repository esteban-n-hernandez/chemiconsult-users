package com.chemiconsult.controller;

import com.chemiconsult.service.ClienteContactoService;
import com.chemiconsult.to.AsignarUsuarioTO;
import com.chemiconsult.to.ClienteContactoTO;
import com.chemiconsult.to.SucursalContactoResumenTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('ADMIN') or hasRole('IT')")
    @PostMapping("/contactos")
    public ResponseEntity<ClienteContactoTO> createContacto(@RequestBody ClienteContactoTO to) {
        return ResponseEntity.status(201).body(contactoService.createContacto(to));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('IT')")
    @PutMapping("/contactos/{id}")
    public ResponseEntity<ClienteContactoTO> updateContacto(@PathVariable Long id, @RequestBody ClienteContactoTO to) {
        return ResponseEntity.ok(contactoService.updateContacto(id, to));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('IT')")
    @PatchMapping("/contactos/{id}/desactivar")
    public ResponseEntity<Void> desactivarContacto(@PathVariable Long id) {
        contactoService.desactivarContacto(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('IT')")
    @PatchMapping("/contactos/{id}/asignar-usuario")
    public ResponseEntity<ClienteContactoTO> asignarUsuario(@PathVariable Long id,
                                                             @RequestBody AsignarUsuarioTO to) {
        return ResponseEntity.ok(contactoService.asignarUsuario(id, to));
    }

    @Autowired
    public ClienteContactoController(ClienteContactoService contactoService) {
        this.contactoService = contactoService;
    }
}