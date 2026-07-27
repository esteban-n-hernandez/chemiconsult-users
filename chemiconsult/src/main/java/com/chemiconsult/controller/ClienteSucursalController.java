ackage com.chemiconsult.controller;

import com.chemiconsult.service.ClienteSucursalService;
import com.chemiconsult.to.ClienteSucursalTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteSucursalController {

    private final ClienteSucursalService sucursalService;

    // GET /api/clientes/{clienteId}/sucursales
    @GetMapping("/{clienteId}/sucursales")
    public ResponseEntity<List<ClienteSucursalTO>> getSucursales(@PathVariable Long clienteId) {
        return ResponseEntity.ok(sucursalService.getSucursalesPorCliente(clienteId));
    }

    // POST /api/clientes/sucursales
    @PostMapping("/sucursales")
    public ResponseEntity<ClienteSucursalTO> createSucursal(@RequestBody ClienteSucursalTO to) {
        return ResponseEntity.status(201).body(sucursalService.createSucursal(to));
    }

    // PUT /api/clientes/sucursales/{id}
    @PutMapping("/sucursales/{id}")
    public ResponseEntity<ClienteSucursalTO> updateSucursal(@PathVariable Long id, @RequestBody ClienteSucursalTO to) {
        return ResponseEntity.ok(sucursalService.updateSucursal(id, to));
    }

    // PATCH /api/clientes/sucursales/{id}/desactivar
    @PatchMapping("/sucursales/{id}/desactivar")
    public ResponseEntity<Void> desactivarSucursal(@PathVariable Long id) {
        sucursalService.desactivarSucursal(id);
        return ResponseEntity.noContent().build();
    }

    @Autowired
    public ClienteSucursalController(ClienteSucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }
}