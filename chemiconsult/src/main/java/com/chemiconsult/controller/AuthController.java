package com.chemiconsult.controller;

import com.chemiconsult.security.JwtUtil;
import com.chemiconsult.service.JwtUserDetailsService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        System.out.println("Login endpoint called - email: " + email);

        // Autenticar al usuario
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // Cargar los detalles del usuario
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(email);

        String role = userDetails.getAuthorities()
            .stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("CLIENTE");

        // Generar el token JWT
        String token = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(new AuthResponse(token, role));
    }

    // Clase interna para la respuesta
    @Setter
    @Getter
    static class AuthResponse {
    private String token;
    private String role;  // 👈 campo nuevo

    public AuthResponse(String token, String role) {
        this.token = token;
        this.role  = role;
    }
}
}