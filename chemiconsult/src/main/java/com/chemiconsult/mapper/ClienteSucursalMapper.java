package com.chemiconsult.mapper;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.ClienteSucursalDE;
import com.chemiconsult.enums.ProvinciaEnum;
import com.chemiconsult.to.ClienteSucursalTO;

import java.time.LocalDate;

public class ClienteSucursalMapper {

    public static ClienteSucursalDE createSucursal(ClienteSucursalTO to, ClienteDE cliente) {
        ClienteSucursalDE sucursal = new ClienteSucursalDE();
        sucursal.setCliente(cliente);
        sucursal.setNombre(to.getNombre());
        sucursal.setDireccion(to.getDireccion());
        sucursal.setLocalidad(to.getLocalidad());
        sucursal.setProvincia(to.getProvincia() != null ? ProvinciaEnum.valueOf(to.getProvincia()) : null);
        sucursal.setActivo(true);
        sucursal.setCreatedDate(LocalDate.now());
        sucursal.setUpdateDate(LocalDate.now());
        return sucursal;
    }

    public static ClienteSucursalDE updateSucursal(ClienteSucursalDE existing, ClienteSucursalTO to) {
        existing.setNombre(to.getNombre());
        existing.setDireccion(to.getDireccion());
        existing.setLocalidad(to.getLocalidad());
        existing.setProvincia(to.getProvincia() != null ? ProvinciaEnum.valueOf(to.getProvincia()) : null);
        existing.setUpdateDate(LocalDate.now());
        return existing;
    }

    public static ClienteSucursalTO mapEntityToTO(ClienteSucursalDE entity) {
        return ClienteSucursalTO.builder()
                .id(entity.getId())
                .clienteId(entity.getCliente() != null ? entity.getCliente().getId() : null)
                .nombre(entity.getNombre())
                .direccion(entity.getDireccion())
                .localidad(entity.getLocalidad())
                .provincia(entity.getProvincia() != null ? entity.getProvincia().name() : null)
                .activo(entity.getActivo())
                .build();
    }
}