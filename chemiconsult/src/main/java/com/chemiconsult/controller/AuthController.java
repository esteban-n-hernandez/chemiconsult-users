package com.chemiconsult.controller;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.enums.ModuloEnum;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.security.JwtUtil;
import com.chemiconsult.security.LoginRateLimiter;
import com.chemiconsult.service.JwtUserDetailsService;
import com.chemiconsult.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import java.util.Set;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserService userService;
    private final LoginRateLimiter rateLimiter;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password,
                                   HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!rateLimiter.intentoPermitido(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Demasiados intentos. Esperá un minuto e intentá de nuevo.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(email);

        UserDE user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String role = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("CLIENTE");

        String token = jwtUtil.generateToken(userDetails.getUsername(), user.getId());

        Set<ModuloEnum> modulos = userService.getModulosForUser(email);

        return ResponseEntity.ok(new AuthResponse(token, role, user.getId(), user.getUsername(), modulos));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refresh(Authentication authentication) {
        String email = authentication.getName();
        UserDE user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        String newToken = jwtUtil.generateToken(email, user.getId());
        return ResponseEntity.ok(Map.of("token", newToken));
    }

    @Setter
    @Getter
    static class AuthResponse {
        private String token;
        private String role;
        private Long userId;
        private String username;
        private Set<ModuloEnum> modulos;

        public AuthResponse(String token, String role, Long userId, String username, Set<ModuloEnum> modulos) {
            this.token = token;
            this.role = role;
            this.userId = userId;
            this.username = username;
            this.modulos = modulos;
        }
    }

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUserDetailsService jwtUserDetailsService, JwtUtil jwtUtil,
                          UserRepository userRepository, UserService userService,
                          LoginRateLimiter rateLimiter) {
        this.authenticationManager = authenticationManager;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userService = userService;
        this.rateLimiter = rateLimiter;
    }
}