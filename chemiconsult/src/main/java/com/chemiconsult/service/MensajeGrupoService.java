package com.chemiconsult.service;

import com.chemiconsult.entity.MensajeGrupoDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.repository.MensajeGrupoRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.MensajeGrupoTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MensajeGrupoService {

    private final MensajeGrupoRepository mensajeGrupoRepository;
    private final UserRepository userRepository;

    @Autowired
    public MensajeGrupoService(MensajeGrupoRepository mensajeGrupoRepository, UserRepository userRepository) {
        this.mensajeGrupoRepository = mensajeGrupoRepository;
        this.userRepository = userRepository;
    }

    public List<MensajeGrupoTO> getUltimos(int limite) {
        List<MensajeGrupoDE> desc = mensajeGrupoRepository.findUltimos(PageRequest.of(0, limite));
        Collections.reverse(desc);
        return desc.stream().map(this::toTO).toList();
    }

    public List<MensajeGrupoTO> getAntesDe(Long antesDeId, int limite) {
        List<MensajeGrupoDE> desc = mensajeGrupoRepository.findAntesDe(antesDeId, PageRequest.of(0, limite));
        Collections.reverse(desc);
        return desc.stream().map(this::toTO).toList();
    }

    public List<MensajeGrupoTO> getDespuesDe(Long despuesDeId) {
        return mensajeGrupoRepository.findDespuesDe(despuesDeId)
                .stream().map(this::toTO).toList();
    }

    public MensajeGrupoTO enviar(Long emisorId, String contenido) {
        UserDE emisor = userRepository.findById(emisorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        MensajeGrupoDE m = new MensajeGrupoDE();
        m.setEmisor(emisor);
        m.setContenido(contenido.trim());
        return toTO(mensajeGrupoRepository.save(m));
    }

    private MensajeGrupoTO toTO(MensajeGrupoDE m) {
        MensajeGrupoTO to = new MensajeGrupoTO();
        to.setId(m.getId());
        to.setEmisorId(m.getEmisor().getId());
        to.setEmisorNombre(m.getEmisor().getUsername());
        to.setContenido(m.getContenido());
        to.setFechaEnvio(m.getFechaEnvio());
        return to;
    }
}
