ackage com.chemiconsult.controller;

import com.chemiconsult.enums.EstadoMuestreoEnum;
import com.chemiconsult.service.MuestreoService;
import com.chemiconsult.to.MuestreoTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/muestreos")
public class MuestreoController {

    private final MuestreoService muestreoService;

    @Autowired
    public MuestreoController(MuestreoService muestreoService) {
        this.muestreoService = muestreoService;
    }

    // GET /api/muestreos  â€” todos, o filtrados por rango de fechas
    @GetMapping
    public ResponseEntity<List<MuestreoTO>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (start != null && end != null) {
            return ResponseEntity.ok(muestreoService.getByRango(start, end));
        }
        return ResponseEntity.ok(muestreoService.getAll());
    }

    // GET /api/muestreos/cliente/{clienteId}
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<MuestreoTO>> getByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(muestreoService.getByCliente(clienteId));
    }

    // GET /api/muestreos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MuestreoTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(muestreoService.getById(id));
    }

    // POST /api/muestreos
    @PostMapping
    public ResponseEntity<MuestreoTO> create(@RequestBody MuestreoTO to) {
        log.info("Creando muestreo para cliente ID: {}", to.getClienteId());
        return ResponseEntity.status(201).body(muestreoService.create(to));
    }

    // PUT /api/muestreos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MuestreoTO> update(@PathVariable Long id, @RequestBody MuestreoTO to) {
        log.info("Actualizando muestreo ID: {}", id);
        return ResponseEntity.ok(muestreoService.update(id, to));
    }

    // PUT /api/muestreos/{id}/estado
    @PutMapping("/{id}/estado")
    public ResponseEntity<MuestreoTO> cambiarEstado(@PathVariable Long id,
                                                     @RequestParam EstadoMuestreoEnum estado) {
        log.info("Cambiando estado de muestreo ID: {} a {}", id, estado);
        return ResponseEntity.ok(muestreoService.cambiarEstado(id, estado));
    }

    // DELETE /api/muestreos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Eliminando muestreo ID: {}", id);
        muestreoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
