package com.chemiconsult.service;

import com.chemiconsult.entity.MensajeDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.repository.MensajeRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.ConversacionTO;
import com.chemiconsult.to.MensajeTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UserRepository userRepository;

    @Autowired
    public MensajeService(MensajeRepository mensajeRepository, UserRepository userRepository) {
        this.mensajeRepository = mensajeRepository;
        this.userRepository = userRepository;
    }

    public List<MensajeTO> getConversacion(Long miId, Long otroId) {
        return mensajeRepository.findConversacion(miId, otroId)
                .stream().map(this::toTO).toList();
    }

    public List<ConversacionTO> getConversaciones(Long miId) {
        List<MensajeDE> todos = mensajeRepository.findTodosDelUsuario(miId);

        Map<Long, MensajeDE> ultimoPorContacto = new LinkedHashMap<>();
        for (MensajeDE m : todos) {
            Long otroId = m.getEmisor().getId().equals(miId)
                    ? m.getReceptor().getId()
                    : m.getEmisor().getId();
            ultimoPorContacto.putIfAbsent(otroId, m);
        }

        List<ConversacionTO> result = new ArrayList<>();
        for (Map.Entry<Long, MensajeDE> entry : ultimoPorContacto.entrySet()) {
            Long otroId = entry.getKey();
            MensajeDE ultimo = entry.getValue();
            UserDE otro = otroId.equals(ultimo.getEmisor().getId())
                    ? ultimo.getEmisor()
                    : ultimo.getReceptor();

            ConversacionTO c = new ConversacionTO();
            c.setOtroUserId(otroId);
            c.setOtroUserNombre(otro.getUsername());
            c.setUltimoMensaje(ultimo.getContenido());
            c.setFechaUltimo(ultimo.getFechaEnvio());
            c.setNoLeidos((int) mensajeRepository.countNoLeidosDe(miId, otroId));
            result.add(c);
        }
        return result;
    }

    public MensajeTO enviar(Long emisorId, Long receptorId, String contenido) {
        UserDE emisor = userRepository.findById(emisorId)
                .orElseThrow(() -> new RuntimeException("Usuario emisor no encontrado"));
        UserDE receptor = userRepository.findById(receptorId)
                .orElseThrow(() -> new RuntimeException("Usuario receptor no encontrado"));

        MensajeDE m = new MensajeDE();
        m.setEmisor(emisor);
        m.setReceptor(receptor);
        m.setContenido(contenido.trim());
        return toTO(mensajeRepository.save(m));
    }

    @Transactional
    public void marcarLeidos(Long miId, Long emisorId) {
        mensajeRepository.marcarLeidosDe(miId, emisorId);
    }

    public long getNoLeidosCount(Long receptorId) {
        return mensajeRepository.countNoLeidos(receptorId);
    }

    private MensajeTO toTO(MensajeDE m) {
        MensajeTO to = new MensajeTO();
        to.setId(m.getId());
        to.setEmisorId(m.getEmisor().getId());
        to.setEmisorNombre(m.getEmisor().getUsername());
        to.setReceptorId(m.getReceptor().getId());
        to.setContenido(m.getContenido());
        to.setLeido(m.isLeido());
        to.setFechaEnvio(m.getFechaEnvio());
        return to;
    }
}
