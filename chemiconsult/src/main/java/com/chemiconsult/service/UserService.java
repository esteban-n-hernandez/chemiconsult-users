package com.chemiconsult.service;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.enums.ModuloEnum;
import com.chemiconsult.mapper.UserMapper;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.CambiarPasswordTO;
import com.chemiconsult.to.UserCreateTO;
import com.chemiconsult.to.UserPerfilTO;
import com.chemiconsult.to.UserTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserTO> getUsers() {
        return UserMapper.mapEntityToUserTOList(
            userRepository.findAll().stream()
                .filter(u -> !"ROLE_CLIENTE".equals(u.getRol()))
                .toList()
        );
    }

    public UserDE getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public UserTO getUserTOById(Long id) {
        return UserMapper.mapEntityToUserTO(getUserById(id));
    }

    public UserDE createUser(UserDE user) {
        // Nunca guardamos password en texto plano, ni siquiera en altas manuales
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public UserTO createEmpleado(UserCreateTO to) {
        if (userRepository.existsByEmail(to.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con el email: " + to.getEmail());
        }
        if (to.getPassword() == null || to.getPassword().length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }
        UserDE user = new UserDE();
        user.setUsername(to.getUsername());
        user.setEmail(to.getEmail());
        user.setPassword(passwordEncoder.encode(to.getPassword()));
        user.setRol(to.getRol());
        user.setCreatedDate(LocalDate.now());
        return UserMapper.mapEntityToUserTO(userRepository.save(user));
    }

    public void resetPassword(Long id, String passwordNueva) {
        if (passwordNueva == null || passwordNueva.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }
        UserDE user = getUserById(id);
        user.setPassword(passwordEncoder.encode(passwordNueva));
        user.setUpdateDate(LocalDate.now());
        userRepository.save(user);
    }

    // Edita username/email — password se maneja aparte en cambiarPassword()
    public UserTO updatePerfil(Long id, UserPerfilTO to) {
        UserDE existing = getUserById(id);

        if (to.getUsername() != null && !to.getUsername().isBlank()
                && !to.getUsername().equals(existing.getUsername())
                && userRepository.existsByUsername(to.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso: " + to.getUsername());
        }

        existing.setUsername(to.getUsername());
        existing.setEmail(to.getEmail());
        existing.setUpdateDate(LocalDate.now());

        return UserMapper.mapEntityToUserTO(userRepository.save(existing));
    }

    // Cambio de contraseña — valida la actual antes de aplicar la nueva
    public void cambiarPassword(Long id, CambiarPasswordTO to) {
        UserDE existing = getUserById(id);

        if (to.getPasswordActual() == null || to.getPasswordNueva() == null || to.getPasswordNueva().isBlank()) {
            throw new RuntimeException("Debe indicar la contraseña actual y la nueva");
        }

        if (!passwordEncoder.matches(to.getPasswordActual(), existing.getPassword())) {
            throw new RuntimeException("La contraseña actual no es correcta");
        }

        if (to.getPasswordNueva().length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        existing.setPassword(passwordEncoder.encode(to.getPasswordNueva()));
        existing.setUpdateDate(LocalDate.now());
        userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Agregar a UserService:

    public List<UserTO> getUsersAsignables() {
        List<String> rolesPermitidos = List.of("ROLE_EMPLEADO", "ROLE_IT", "EMPLEADO", "IT");
        return UserMapper.mapEntityToUserTOList(userRepository.findByRolIn(rolesPermitidos));
    }

    public Set<ModuloEnum> getModulosForUser(String email) {
        UserDE user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        // IT siempre recibe todos los módulos
        if ("ROLE_IT".equalsIgnoreCase(user.getRol()) || "IT".equalsIgnoreCase(user.getRol())) {
            return EnumSet.allOf(ModuloEnum.class);
        }
        return user.getModulos();
    }

    public Set<ModuloEnum> setModulos(Long id, Set<ModuloEnum> modulos) {
        UserDE user = getUserById(id);
        user.getModulos().clear();
        if (modulos != null) user.getModulos().addAll(modulos);
        user.setUpdateDate(LocalDate.now());
        return userRepository.save(user).getModulos();
    }

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}