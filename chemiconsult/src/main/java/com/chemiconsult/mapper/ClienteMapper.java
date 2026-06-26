package com.chemiconsult.mapper;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.to.ClienteTO;

import java.time.LocalDate;

public class ClienteMapper {

    public static ClienteDE createCliente(ClienteTO to) {
        ClienteDE cliente = new ClienteDE();
        cliente.setTipoCliente(to.getTipoCliente());

        // Persona Física
        cliente.setNombre(to.getNombre());
        cliente.setApellido(to.getApellido());
        cliente.setDni(to.getDni());
        cliente.setCuil(to.getCuil());

        // Empresa
        cliente.setRazonSocial(to.getRazonSocial());
        cliente.setCuit(to.getCuit());

        // Compartidos
        cliente.setEmail(to.getEmail());
        cliente.setTelefono(to.getTelefono());
        cliente.setCelular(to.getCelular());
        cliente.setDireccion(to.getDireccion());
        cliente.setLocalidad(to.getLocalidad());
        cliente.setProvincia(to.getProvincia());
        cliente.setCondicionIVA(to.getCondicionIVA());

        // Sistema
        cliente.setActivo(true);
        cliente.setCreatedDate(LocalDate.now());
        cliente.setUpdateDate(LocalDate.now());

        return cliente;
    }

    public static ClienteDE updateCliente(ClienteDE existing, ClienteTO to) {
        existing.setTipoCliente(to.getTipoCliente());

        // Persona Física
        existing.setNombre(to.getNombre());
        existing.setApellido(to.getApellido());
        existing.setDni(to.getDni());
        existing.setCuil(to.getCuil());

        // Empresa
        existing.setRazonSocial(to.getRazonSocial());
        existing.setCuit(to.getCuit());

        // Compartidos
        existing.setEmail(to.getEmail());
        existing.setTelefono(to.getTelefono());
        existing.setCelular(to.getCelular());
        existing.setDireccion(to.getDireccion());
        existing.setLocalidad(to.getLocalidad());
        existing.setProvincia(to.getProvincia());
        existing.setCondicionIVA(to.getCondicionIVA());

        existing.setUpdateDate(LocalDate.now());

        return existing;
    }
}
