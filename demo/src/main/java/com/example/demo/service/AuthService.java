package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.authen.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
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

    @Transactional
    public void register(AuthRegisterRequest req) {

        repo.findByEmail(req.getEmail()).ifPresent(u -> {
            log.info("Phát hiện hồ sơ chưa xác thực, xoá trước khi tạo mới | e={}", anEmail(req.getEmail()));
            repo.delete(u);
        });

        if (repo.existsByEmail(req.getEmail())) {
            log.warn("Từ chối đăng ký: email đã được xác thực và đang tồn tại | e={}", anEmail(req.getEmail()));
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (repo.existsByUsername(req.getUsername())) {
            log.warn("Từ chối đăng ký: tên đăng nhập đã tồn tại (đã xác thực) | u={}", anUser(req.getUsername()));
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        Instant now = Instant.now();
        String token = UUID.randomUUID().toString().replace("-", "");

        AppUser u = AppUser.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .roles(Set.of("USER"))
                .enabled(true)
                .emailVerified(false)
                .verificationToken(token)
                .verificationExpiry(now.plus(expireMin, ChronoUnit.MINUTES))
                .createdAt(now).updatedAt(now)
                .build();
        repo.save(u);

        String verifyUrl = "%s/api/auth/verify?token=%s".formatted(baseUrl, token);
        emailService.sendVerificationEmail(u.getEmail(), verifyUrl);

        log.info("Đăng ký mới thành công: đã gửi email xác thực | u={}, hiệu lực {} phút",
                anUser(u.getUsername()), expireMin);
    }

    @Transactional
    public VerifyResponse verifyEmail(String token) {
        var user = repo.findByVerificationToken(token).orElseThrow(() -> {
            log.warn("Xác thực email thất bại: token không hợp lệ");
            return new IllegalArgumentException("Token không hợp lệ");
        });

        if (user.getVerificationExpiry() == null || user.getVerificationExpiry().isBefore(Instant.now())) {
            log.warn("Xác thực email thất bại: token đã hết hạn | u={}", anUser(user.getUsername()));
            throw new IllegalArgumentException("Token đã hết hạn");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpiry(null);
        user.setUpdatedAt(Instant.now());
        repo.save(user);

        log.info("Xác thực email thành công | u={}", anUser(user.getUsername()));
        return VerifyResponse.builder().success(true).message("Xác thực thành công").build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest req, long expiresInMs) {
        var u = repo.findByUsername(req.getUsername()).orElseThrow(() -> {
            log.warn("Đăng nhập thất bại: sai tên đăng nhập hoặc mật khẩu | u={}", anUser(req.getUsername()));
            return new BadCredentialsException("Sai username hoặc password");
        });

        if (Boolean.FALSE.equals(u.getEmailVerified())) {
            log.warn("Đăng nhập bị từ chối: tài khoản chưa xác thực email | u={}", anUser(u.getUsername()));
            throw new BadCredentialsException("Tài khoản chưa xác thực email");
        }

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            String user = authentication.getName();
            var dbUser = repo.findByUsername(user).orElseThrow();
            String token = jwtUtil.generate(user, Map.of("roles", dbUser.getRoles()));

            log.info("Đăng nhập thành công | u={}", anUser(user));
            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(expiresInMs)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Đăng nhập thất bại: sai tên đăng nhập hoặc mật khẩu | u={}", anUser(req.getUsername()));
            throw e;
        } catch (DisabledException e) {
            log.warn("Đăng nhập thất bại: tài khoản bị vô hiệu hóa | u={}", anUser(req.getUsername()));
            throw e;
        } catch (LockedException e) {
            log.warn("Đăng nhập thất bại: tài khoản bị khóa | u={}", anUser(req.getUsername()));
            throw e;
        } catch (AuthenticationException e) {
            log.warn("Đăng nhập thất bại: lỗi xác thực ({}) | u={}",
                    e.getClass().getSimpleName(), anUser(req.getUsername()));
            throw e;
        } catch (Exception e) {
            log.error("Đăng nhập lỗi không xác định | u={}", anUser(req.getUsername()), e);
            throw e;
        }
    }

    // ===== Helpers: ẩn/mask thông tin nhạy cảm trong log =====
    private String anEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String dom = parts[1];
        int head = Math.min(2, name.length());
        return name.substring(0, head) + "***@" + dom;
    }

    private String anUser(String username) {
        if (username == null || username.isBlank()) return "***";
        int keep = Math.min(2, username.length());
        return username.substring(0, keep) + "***";
    }
}
