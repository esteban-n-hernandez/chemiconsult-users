package com.chemiconsult.service;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.ClienteSucursalDE;
import com.chemiconsult.entity.MuestreoDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.enums.EstadoMuestreoEnum;
import com.chemiconsult.enums.TipoEventoAgendaEnum;
import com.chemiconsult.mapper.MuestreoMapper;
import com.chemiconsult.repository.ClienteRepository;
import com.chemiconsult.repository.ClienteSucursalRepository;
import com.chemiconsult.repository.MuestreoRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.MuestreoTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MuestreoService {

    private final MuestreoRepository muestreoRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteSucursalRepository sucursalRepository;
    private final UserRepository userRepository;

    @Autowired
    public MuestreoService(MuestreoRepository muestreoRepository,
                           ClienteRepository clienteRepository,
                           ClienteSucursalRepository sucursalRepository,
                           UserRepository userRepository) {
        this.muestreoRepository = muestreoRepository;
        this.clienteRepository = clienteRepository;
        this.sucursalRepository = sucursalRepository;
        this.userRepository = userRepository;
    }

    public List<MuestreoTO> getAll() {
        return MuestreoMapper.toTO(muestreoRepository.findAll());
    }

    public List<MuestreoTO> getByRango(LocalDateTime start, LocalDateTime end) {
        return MuestreoMapper.toTO(muestreoRepository.findByFechaHoraBetween(start, end));
    }

    public List<MuestreoTO> getByCliente(Long clienteId) {
        return MuestreoMapper.toTO(muestreoRepository.findByCliente_Id(clienteId));
    }

    public MuestreoTO getById(Long id) {
        return MuestreoMapper.toTO(findOrThrow(id));
    }

    public MuestreoTO create(MuestreoTO to) {
        MuestreoDE entity = new MuestreoDE();
        applyFields(entity, to);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        return MuestreoMapper.toTO(muestreoRepository.save(entity));
    }

    public MuestreoTO update(Long id, MuestreoTO to) {
        MuestreoDE entity = findOrThrow(id);
        applyFields(entity, to);
        entity.setUpdateDate(LocalDateTime.now());
        return MuestreoMapper.toTO(muestreoRepository.save(entity));
    }

    public MuestreoTO cambiarEstado(Long id, EstadoMuestreoEnum estado) {
        MuestreoDE entity = findOrThrow(id);
        entity.setEstado(estado);
        entity.setUpdateDate(LocalDateTime.now());
        return MuestreoMapper.toTO(muestreoRepository.save(entity));
    }

    public void delete(Long id) {
        if (!muestreoRepository.existsById(id)) {
            throw new RuntimeException("Muestreo no encontrado con ID: " + id);
        }
        muestreoRepository.deleteById(id);
    }

    private void applyFields(MuestreoDE entity, MuestreoTO to) {
        entity.setTipo(to.getTipo() != null
                ? TipoEventoAgendaEnum.valueOf(to.getTipo())
                : TipoEventoAgendaEnum.MUESTREO);

        if (to.getClienteId() != null) {
            ClienteDE cliente = clienteRepository.findById(to.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + to.getClienteId()));
            entity.setCliente(cliente);
        } else {
            entity.setCliente(null);
        }

        if (to.getSucursalId() != null) {
            ClienteSucursalDE sucursal = sucursalRepository.findById(to.getSucursalId())
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + to.getSucursalId()));
            entity.setSucursal(sucursal);
        } else {
            entity.setSucursal(null);
        }

        if (to.getResponsableId() != null) {
            UserDE responsable = userRepository.findById(to.getResponsableId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + to.getResponsableId()));
            entity.setResponsable(responsable);
        } else {
            entity.setResponsable(null);
        }

        entity.setFechaHora(LocalDateTime.parse(to.getFechaHora()));
        entity.setDireccion(to.getDireccion());
        entity.setObservaciones(to.getObservaciones());

        if (to.getEstado() != null) {
            entity.setEstado(EstadoMuestreoEnum.valueOf(to.getEstado()));
        } else {
            entity.setEstado(EstadoMuestreoEnum.PENDIENTE);
        }
    }

    private MuestreoDE findOrThrow(Long id) {
        return muestreoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Muestreo no encontrado con ID: " + id));
    }
}
