package com.chemiconsult.service;

import com.chemiconsult.entity.AnalisisParametroDE;
import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.enums.EstadoAnalisisParametroEnum;
import com.chemiconsult.enums.EstadoMuestraEnum;
import com.chemiconsult.repository.AnalisisParametroRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.ColaAnalisisTO;
import com.chemiconsult.to.ColaMuestraTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ColaAnalisisService {

    private static final List<EstadoMuestraEnum> EXCLUIDOS =
            List.of(EstadoMuestraEnum.COMPLETO, EstadoMuestraEnum.CANCELADO);

    @Autowired private AnalisisParametroRepository analisisParametroRepository;
    @Autowired private UserRepository              userRepository;

    @Transactional(readOnly = true)
    public List<ColaAnalisisTO> getMiCola(String username) {
        UserDE user = findUser(username);
        List<AnalisisParametroDE> pendientes =
                analisisParametroRepository.findPendientesByResponsableId(
                        user.getId(), EXCLUIDOS, EstadoAnalisisParametroEnum.PENDIENTE);
        return agruparPorParametro(pendientes);
    }

    @Transactional(readOnly = true)
    public List<ColaAnalisisTO> getTodos(String username) {
        UserDE user = findUser(username);
        List<AnalisisParametroDE> todos =
                analisisParametroRepository.findTodosByResponsableId(user.getId(), EXCLUIDOS);
        return agruparPorParametro(todos);
    }

    @Transactional(readOnly = true)
    public List<ColaAnalisisTO> getTodosGlobal() {
        List<AnalisisParametroDE> todos =
                analisisParametroRepository.findAllExcluidos(EXCLUIDOS);
        return agruparPorParametro(todos);
    }

    @Transactional
    public void cambiarEstado(Long analisisParametroId, EstadoAnalisisParametroEnum nuevoEstado, String username) {
        AnalisisParametroDE ap = analisisParametroRepository.findById(analisisParametroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem no encontrado"));
        ap.setEstadoAnalisis(nuevoEstado);
        analisisParametroRepository.save(ap);
    }

    // ----------------------------------------------------------------

    private List<ColaAnalisisTO> agruparPorParametro(List<AnalisisParametroDE> lista) {
        Map<Long, List<AnalisisParametroDE>> porParametro = lista.stream()
                .collect(Collectors.groupingBy(ap -> ap.getParametro().getId()));

        return porParametro.values().stream()
                .map(grupo -> {
                    AnalisisParametroDE primero = grupo.get(0);
                    UserDE responsable = primero.getParametro().getResponsable();

                    ColaAnalisisTO cola = new ColaAnalisisTO();
                    cola.setParametroId(primero.getParametro().getId());
                    cola.setParametroNombre(primero.getParametro().getNombre());
                    cola.setUnidad(primero.getParametro().getUnidad());
                    if (responsable != null) {
                        cola.setResponsableId(responsable.getId());
                        cola.setResponsableNombre(
                                responsable.getUsername() != null ? responsable.getUsername() : responsable.getEmail());
                    }
                    cola.setTotalPendientes((int) grupo.stream()
                            .filter(ap -> estadoSafe(ap) == EstadoAnalisisParametroEnum.PENDIENTE).count());
                    cola.setTotalAnalizado((int) grupo.stream()
                            .filter(ap -> estadoSafe(ap) == EstadoAnalisisParametroEnum.ANALIZADO).count());
                    cola.setTotalConfirmado((int) grupo.stream()
                            .filter(ap -> estadoSafe(ap) == EstadoAnalisisParametroEnum.CONFIRMADO).count());
                    cola.setTotalObservado((int) grupo.stream()
                            .filter(ap -> estadoSafe(ap) == EstadoAnalisisParametroEnum.REPETIR).count());
                    cola.setMuestras(grupo.stream().map(this::toColaMuestra).collect(Collectors.toList()));
                    return cola;
                })
                .sorted(Comparator.comparing(ColaAnalisisTO::getParametroNombre))
                .collect(Collectors.toList());
    }

    private EstadoAnalisisParametroEnum estadoSafe(AnalisisParametroDE ap) {
        return ap.getEstadoAnalisis() != null ? ap.getEstadoAnalisis() : EstadoAnalisisParametroEnum.PENDIENTE;
    }

    private ColaMuestraTO toColaMuestra(AnalisisParametroDE ap) {
        ColaMuestraTO m = new ColaMuestraTO();
        m.setAnalisisParametroId(ap.getId());
        m.setAnalisisId(ap.getAnalisis().getId());
        m.setNroProtocolo(ap.getAnalisis().getNumeroProtocolo());
        m.setEstado(ap.getAnalisis().getEstado().name());
        m.setPuntoMuestreo(ap.getAnalisis().getPuntoMuestreo());
        m.setEstadoAnalisis(estadoSafe(ap).name());
        if (ap.getAnalisis().getFechaEntrega() != null) {
            m.setFechaEntrega(ap.getAnalisis().getFechaEntrega().toString());
        }
        ClienteDE cliente = ap.getAnalisis().getCliente();
        if (cliente != null) {
            String rs = cliente.getRazonSocial();
            if (rs != null && !rs.isBlank()) {
                m.setClienteNombre(rs);
            } else {
                String nombre = (cliente.getNombre() != null ? cliente.getNombre() : "") +
                                (cliente.getApellido() != null ? " " + cliente.getApellido() : "");
                m.setClienteNombre(nombre.isBlank() ? cliente.getEmail() : nombre.trim());
            }
        }
        return m;
    }

    private UserDE findUser(String username) {
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }
}
