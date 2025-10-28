package com.example.demo.config;

import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevAdminSeeder implements CommandLineRunner {
    private final AppUserRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!repo.existsByUsername("admin")) {
            var now = Instant.now();
            repo.save(AppUser.builder()
                    .username("admin")
                    .password(encoder.encode("admin123"))
                    .roles(Set.of("ADMIN","USER"))
                    .enabled(true)
                    .createdAt(now).updatedAt(now)
                    .build());
            System.out.println("✅ Seeded admin/admin123");
        }
    }
}
