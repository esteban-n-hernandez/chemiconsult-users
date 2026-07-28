package com.chemiconsult.service;

import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.enums.RolEnum;
import com.chemiconsult.mapper.ClienteMapper;
import com.chemiconsult.repository.ClienteRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.AsignarUsuarioTO;
import com.chemiconsult.to.ClienteTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Solo activos ──
    public List<ClienteDE> getClientes() {
        return clienteRepository.findByActivoTrue();
    }

    // ── Todos ──
    public List<ClienteDE> getClientesTodos() {
        return clienteRepository.findAll();
    }

    // ── Por ID ──
    public ClienteDE getCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    // ── Crear ──
    public ClienteDE createCliente(ClienteTO to) {
        // Validar email único
        if (clienteRepository.existsByEmailIgnoreCase(to.getEmail())) {
            throw new RuntimeException("Ya existe un cliente con el email: " + to.getEmail());
        }

        // Validar CUIT único si viene
        if (to.getCuit() != null && clienteRepository.existsByCuit(to.getCuit())) {
            throw new RuntimeException("Ya existe un cliente con el CUIT: " + to.getCuit());
        }

        // Validar DNI único si viene
        if (to.getDni() != null && clienteRepository.existsByDni(to.getDni())) {
            throw new RuntimeException("Ya existe un cliente con el DNI: " + to.getDni());
        }

        ClienteDE cliente = ClienteMapper.createCliente(to);
        return clienteRepository.save(cliente);
    }

    // ── Actualizar ──
    public ClienteDE updateCliente(Long id, ClienteTO to) {
        ClienteDE existing = getCliente(id);
        ClienteDE actualizado = ClienteMapper.updateCliente(existing, to);
        return clienteRepository.save(actualizado);
    }

    // ── Desactivar (baja lógica) ──
    public void desactivarCliente(Long id) {
        ClienteDE cliente = getCliente(id);
        cliente.setActivo(false);
        cliente.setUpdateDate(LocalDate.now());
        clienteRepository.save(cliente);
    }

    // ── Reactivar ──
    public void reactivarCliente(Long id) {
        ClienteDE cliente = getCliente(id);
        cliente.setActivo(true);
        cliente.setUpdateDate(LocalDate.now());
        clienteRepository.save(cliente);
    }

    // ── Eliminar físico ──
    public void deleteCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
        clienteRepository.deleteById(id);
    }

    // ── Cliente por usuario logueado ──
    public ClienteDE getClientePorEmail(String email) {
        UserDE user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        return clienteRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("No hay cliente asociado a este usuario"));
    }

    // ── Asignar usuario al cliente ──
    public ClienteDE asignarUsuario(Long id, AsignarUsuarioTO to) {
        ClienteDE cliente = getCliente(id);

        // Verificar que no tenga usuario ya asignado
        if (cliente.getUser() != null) {
            throw new RuntimeException("El cliente ya tiene un usuario asignado");
        }

        // Verificar que el email del cliente no esté ya registrado como usuario
        if (userRepository.existsByEmail(cliente.getEmail())) {
            throw new RuntimeException("El email del cliente ya tiene un usuario registrado");
        }

        UserDE user = new UserDE();
        user.setUsername(cliente.getNombre());
        user.setPassword(passwordEncoder.encode(to.getPassword()));
        user.setEmail(cliente.getEmail());
        user.setRol(RolEnum.ROLE_CLIENTE.name());
        userRepository.save(user);

        cliente.setUser(user);
        cliente.setUpdateDate(LocalDate.now());
        return clienteRepository.save(cliente);
    }
}
