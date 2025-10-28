package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.authen.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public void register(AuthRegisterRequest req) {
        if (repo.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username đã tồn tại");
        var now = Instant.now();
        var u = AppUser.builder()
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword()))
                .roles(Set.of("USER"))
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        repo.save(u);
    }

    public AuthResponse login(AuthLoginRequest req, long expiresInMs) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        var user = authentication.getName();
        var dbUser = repo.findByUsername(user).orElseThrow();
        var token = jwtUtil.generate(user, Map.of("roles", dbUser.getRoles()));
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expiresInMs)
                .build();
    }
}
