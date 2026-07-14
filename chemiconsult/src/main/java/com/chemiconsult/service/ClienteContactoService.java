package com.chemiconsult.service;

import com.chemiconsult.entity.*;
import com.chemiconsult.mapper.ClienteContactoMapper;
import com.chemiconsult.repository.*;
import com.chemiconsult.to.ClienteContactoTO;
import com.chemiconsult.to.SucursalContactoResumenTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClienteContactoService {

    private final ClienteContactoRepository contactoRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteSucursalRepository sucursalRepository;
    private final ClienteSucursalContactoRepository sucursalContactoRepository;

    public List<ClienteContactoTO> getContactosPorCliente(Long clienteId) {
        return contactoRepository.findByClienteIdAndActivoTrue(clienteId)
                .stream()
                .map(ClienteContactoMapper::mapEntityToTO)
                .toList();
    }

    // Devuelve los contactos asociados a UNA sucursal puntual (para saber a quién avisar)
    public List<SucursalContactoResumenTO> getContactosPorSucursal(Long sucursalId) {
        return sucursalContactoRepository.findBySucursalId(sucursalId)
                .stream()
                .map(rel -> ClienteContactoMapper.mapContactoToResumen(rel.getContacto()))
                .toList();
    }

    @Transactional
    public ClienteContactoTO createContacto(ClienteContactoTO to) {
        ClienteDE cliente = clienteRepository.findById(to.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        ClienteContactoDE contacto = ClienteContactoMapper.createContacto(to, cliente);
        contacto = contactoRepository.save(contacto);

        vincularSucursales(contacto, to.getSucursalIds());

        return ClienteContactoMapper.mapEntityToTO(contacto);
    }

    @Transactional
    public ClienteContactoTO updateContacto(Long id, ClienteContactoTO to) {
        ClienteContactoDE existing = contactoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado"));

        ClienteContactoDE actualizado = ClienteContactoMapper.updateContacto(existing, to);

        // Resetea los vínculos y los vuelve a armar según lo que llegó en el TO
        if (to.getSucursalIds() != null) {
            actualizado.getSucursalesAsociadas().clear();
            actualizado = contactoRepository.save(actualizado); // flush del clear antes de re-vincular
            vincularSucursales(actualizado, to.getSucursalIds());
        } else {
            actualizado = contactoRepository.save(actualizado);
        }

        return ClienteContactoMapper.mapEntityToTO(actualizado);
    }

    public void desactivarContacto(Long id) {
        ClienteContactoDE contacto = contactoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado"));
        contacto.setActivo(false);
        contacto.setUpdateDate(LocalDate.now());
        contactoRepository.save(contacto);
    }

    private void vincularSucursales(ClienteContactoDE contacto, List<Long> sucursalIds) {
        if (sucursalIds == null) return;

        List<ClienteSucursalContactoDE> vinculos = new ArrayList<>();
        for (Long sucursalId : sucursalIds) {
            ClienteSucursalDE sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada: " + sucursalId));

            ClienteSucursalContactoDE vinculo = new ClienteSucursalContactoDE();
            vinculo.setSucursal(sucursal);
            vinculo.setContacto(contacto);
            vinculos.add(vinculo);
        }
        sucursalContactoRepository.saveAll(vinculos);
    }

    @Autowired
    public ClienteContactoService(ClienteContactoRepository contactoRepository,
                                  ClienteRepository clienteRepository,
                                  ClienteSucursalRepository sucursalRepository,
                                  ClienteSucursalContactoRepository sucursalContactoRepository) {
        this.contactoRepository = contactoRepository;
        this.clienteRepository = clienteRepository;
        this.sucursalRepository = sucursalRepository;
        this.sucursalContactoRepository = sucursalContactoRepository;
    }
}