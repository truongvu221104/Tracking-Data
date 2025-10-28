package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.authen.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${app.verification.base-url}")
    private String baseUrl;

    @Value("${app.verification.expire-min:60}")
    private long expireMin;

    public void register(AuthRegisterRequest req) {
        if (repo.existsByUsername(req.getUsername())) throw new IllegalArgumentException("Username đã tồn tại");
        if (repo.existsByEmail(req.getEmail()))       throw new IllegalArgumentException("Email đã tồn tại");

        var now = Instant.now();
        var token = UUID.randomUUID().toString().replace("-", "");
        var u = AppUser.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .roles(Set.of("USER"))
                .enabled(true)                // vẫn true để truy cập /auth/verify, nhưng login sẽ bị chặn nếu chưa verify
                .emailVerified(false)
                .verificationToken(token)
                .verificationExpiry(now.plus(expireMin, ChronoUnit.MINUTES))
                .createdAt(now).updatedAt(now)
                .build();
        repo.save(u);

        var verifyUrl = "%s/api/auth/verify?token=%s".formatted(baseUrl, token);
        emailService.sendVerificationEmail(u.getEmail(), verifyUrl);
    }

    public VerifyResponse verifyEmail(String token) {
        var user = repo.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));
        if (user.getVerificationExpiry() == null || user.getVerificationExpiry().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpiry(null);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
        return VerifyResponse.builder().success(true).message("Xác thực thành công").build();
    }

    public AuthResponse login(AuthLoginRequest req, long expiresInMs) {
        // từ chối login nếu email chưa verify
        var u = repo.findByUsername(req.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Sai username hoặc password"));
        if (Boolean.FALSE.equals(u.getEmailVerified())) {
            throw new BadCredentialsException("Tài khoản chưa xác thực email");
        }
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
