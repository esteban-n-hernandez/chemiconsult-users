package com.chemiconsult.mapper;

import com.chemiconsult.entity.ClienteContactoDE;
import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.to.ClienteContactoTO;
import com.chemiconsult.to.SucursalContactoResumenTO;

import java.time.LocalDate;
import java.util.List;

public class ClienteContactoMapper {

    public static ClienteContactoDE createContacto(ClienteContactoTO to, ClienteDE cliente) {
        ClienteContactoDE contacto = new ClienteContactoDE();
        contacto.setCliente(cliente);
        contacto.setNombre(to.getNombre());
        contacto.setEmail(to.getEmail());
        contacto.setTelefono(to.getTelefono());
        contacto.setActivo(true);
        contacto.setCreatedDate(LocalDate.now());
        contacto.setUpdateDate(LocalDate.now());
        return contacto;
    }

    public static ClienteContactoDE updateContacto(ClienteContactoDE existing, ClienteContactoTO to) {
        existing.setNombre(to.getNombre());
        existing.setEmail(to.getEmail());
        existing.setTelefono(to.getTelefono());
        existing.setUpdateDate(LocalDate.now());
        return existing;
    }

    public static ClienteContactoTO mapEntityToTO(ClienteContactoDE entity) {
        List<Long> sucursalIds = entity.getSucursalesAsociadas() == null
                ? List.of()
                : entity.getSucursalesAsociadas().stream()
                .map(rel -> rel.getSucursal().getId())
                .toList();

        return ClienteContactoTO.builder()
                .id(entity.getId())
                .clienteId(entity.getCliente() != null ? entity.getCliente().getId() : null)
                .nombre(entity.getNombre())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .activo(entity.getActivo())
                .build(); // sucursalIds se completa aparte en el service (ver abajo)
    }

    public static SucursalContactoResumenTO mapContactoToResumen(ClienteContactoDE entity) {
        return SucursalContactoResumenTO.builder()
                .contactoId(entity.getId())
                .nombre(entity.getNombre())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .build();
    }
}