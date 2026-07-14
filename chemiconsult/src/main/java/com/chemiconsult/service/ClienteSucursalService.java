package com.chemiconsult.service;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.ClienteSucursalDE;
import com.chemiconsult.mapper.ClienteSucursalMapper;
import com.chemiconsult.repository.ClienteRepository;
import com.chemiconsult.repository.ClienteSucursalRepository;
import com.chemiconsult.to.ClienteSucursalTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClienteSucursalService {

    private final ClienteSucursalRepository sucursalRepository;
    private final ClienteRepository clienteRepository;

    public List<ClienteSucursalTO> getSucursalesPorCliente(Long clienteId) {
        return sucursalRepository.findByClienteIdAndActivoTrue(clienteId)
                .stream()
                .map(ClienteSucursalMapper::mapEntityToTO)
                .toList();
    }

    public ClienteSucursalTO createSucursal(ClienteSucursalTO to) {
        ClienteDE cliente = clienteRepository.findById(to.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        ClienteSucursalDE sucursal = ClienteSucursalMapper.createSucursal(to, cliente);
        return ClienteSucursalMapper.mapEntityToTO(sucursalRepository.save(sucursal));
    }

    public ClienteSucursalTO updateSucursal(Long id, ClienteSucursalTO to) {
        ClienteSucursalDE existing = sucursalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        ClienteSucursalDE actualizada = ClienteSucursalMapper.updateSucursal(existing, to);
        return ClienteSucursalMapper.mapEntityToTO(sucursalRepository.save(actualizada));
    }

    public void desactivarSucursal(Long id) {
        ClienteSucursalDE sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        sucursal.setActivo(false);
        sucursal.setUpdateDate(LocalDate.now());
        sucursalRepository.save(sucursal);
    }

    @Autowired
    public ClienteSucursalService(ClienteSucursalRepository sucursalRepository,
                                  ClienteRepository clienteRepository) {
        this.sucursalRepository = sucursalRepository;
        this.clienteRepository = clienteRepository;
    }
}