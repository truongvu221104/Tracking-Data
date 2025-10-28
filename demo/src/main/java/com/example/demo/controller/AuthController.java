package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository userRepo;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Validated AuthRegisterRequest req) {
        authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Validated AuthLoginRequest req) {
        return authService.login(req, expirationMs);
    }

    @GetMapping("/me")
    public AppUser me(@AuthenticationPrincipal User principal) {
        // Trả về thông tin user DB (ẩn password nhờ @JsonIgnore nếu muốn)
        return userRepo.findByUsername(principal.getUsername()).orElseThrow();
    }
}
