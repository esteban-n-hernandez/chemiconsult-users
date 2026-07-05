package com.chemiconsult.mapper;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.to.EstudioTO;

import java.time.LocalDate;

public class EstudiosMapper {

    public static AnalisisDE createEstudio(EstudioTO estudio, UserDE user) {
        AnalisisDE entity = new AnalisisDE();
        entity.setTipo(estudio.getTipo());
        entity.setEstado(estudio.getEstado());
        entity.setArchivo(estudio.getArchivo());
        entity.setUser(user);
        entity.setCreatedDate(LocalDate.now());

        return entity;
    }


    public static EstudioTO mapEntityToEstudioTOByID(AnalisisDE entity) {
        return EstudioTO.builder()
                .id(entity.getId())
                .archivo(entity.getArchivo())
                .archivoUrl(entity.getArchivoUrl())
                .estado(entity.getEstado())
                .tipo(entity.getTipo())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userMail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .build();
    }

    public static EstudioTO mapEntityToEstudioTO(AnalisisDE entity) {
        return EstudioTO.builder()
                .id(entity.getId())
                .archivo(entity.getArchivo())
                .archivoUrl(entity.getArchivoUrl())
                .estado(entity.getEstado())
                .tipo(entity.getTipo())
                .createdDate(String.valueOf(entity.getCreatedDate()))
                .build();
    }

    public static EstudioTO mapEntityToEstudioTO(AnalisisDE entity, ClienteDE cliente) {
        String clienteStr = null;
        if (cliente != null) {
            if (cliente.getTipoCliente() != null && cliente.getTipoCliente().name().equals("PERSONA_FISICA")) {
                clienteStr = (cliente.getNombre() != null ? cliente.getNombre() : "")
                        + (cliente.getApellido() != null ? " " + cliente.getApellido() : "");
            } else {
                clienteStr = cliente.getRazonSocial();
            }
        }
        if (clienteStr == null || clienteStr.isBlank()) {
            if (entity.getUser() != null) {
                clienteStr = entity.getUser().getUsername() + "/" + entity.getUser().getEmail();
            } else {
                clienteStr = "";
            }
        }

        return EstudioTO.builder()
                .id(entity.getId())
                .cliente(clienteStr)
                .archivo(entity.getArchivo())
                .archivoUrl(entity.getArchivoUrl())
                .estado(entity.getEstado())
                .tipo(entity.getTipo())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userMail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .build();
    }

}
