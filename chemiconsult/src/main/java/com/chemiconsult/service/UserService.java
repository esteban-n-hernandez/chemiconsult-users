package com.chemiconsult.service;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.mapper.UserMapper;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.UserTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserTO> getUsers() {
        return UserMapper.mapEntityToUserTOList(userRepository.findAll());
    }

    public UserDE getUserById(Long id) {
        return userRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public UserDE createUser(UserDE user) {
        return userRepository.save(user);
    }

    public UserDE updateUser(Long id, @RequestBody UserDE user) {
        Optional<UserDE> optional = userRepository.findById(Math.toIntExact(id));
        if (optional.isPresent()) {
            UserDE existing = optional.get();
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setPassword(user.getPassword());
            existing.setRol(user.getRol());
            return userRepository.save(existing);
        }
        throw new RuntimeException("Usuario no encontrado con ID: " + id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(Math.toIntExact(id));
    }
}
