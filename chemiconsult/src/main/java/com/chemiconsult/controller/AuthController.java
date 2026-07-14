package com.chemiconsult.controller;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {

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

        // FIX: agregamos username — el front lo necesita para mostrar el nombre correcto
        return ResponseEntity.ok(new AuthResponse(token, role, user.getId(), user.getUsername()));
    }

    @Setter
    @Getter
    static class AuthResponse {
        private String token;
        private String role;
        private Long userId;
        private String username;

        public AuthResponse(String token, String role, Long userId, String username) {
            this.token = token;
            this.role = role;
            this.userId = userId;
            this.username = username;
        }
    }

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUserDetailsService jwtUserDetailsService, JwtUtil jwtUtil,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }
}