package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.model.SocialAccount;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepo;
    private final SocialAccountRepository socialRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Tìm user từ thông tin social, nếu chưa có thì:
     *  - Nếu email trùng user tồn tại: link social vào user đó
     *  - Nếu email mới: tạo user mới + link social
     */
    public AppUser findOrCreateFromSocial(
            String provider,
            String providerUserId,
            String email,
            String name
    ) {
        // 1. đã có SocialAccount chưa?
        SocialAccount social = socialRepo
                .findByProviderAndProviderUserId(provider, providerUserId)
                .orElse(null);

        if (social != null) {
            return social.getUser();
        }

        // 2. chưa có social, thử tìm user theo email
        AppUser user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            // 3. tạo user mới
            String username = genUsernameFromEmailOrName(email, name);

            user = AppUser.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // random, user không dùng tới
                    .roles(Set.of("USER"))
                    .enabled(true)
                    .emailVerified(true) // social đã verify email giúp mình rồi (tùy bạn)
                    .createdAt(Instant.now())
                    .build();
            user = userRepo.save(user);
        }

        // 4. tạo SocialAccount link với user
        SocialAccount newSocial = SocialAccount.builder()
                .provider(provider.toUpperCase())
                .providerUserId(providerUserId)
                .user(user)
                .build();
        socialRepo.save(newSocial);

        return user;
    }

    private String genUsernameFromEmailOrName(String email, String name) {
        if (email != null && !email.isBlank()) {
            String base = email.split("@")[0];
            String username = base;
            int i = 1;
            while (userRepo.findByUsername(username).isPresent()) {
                username = base + i;
                i++;
            }
            return username;
        }
        if (name != null && !name.isBlank()) {
            return name.replaceAll("\\s+", "").toLowerCase();
        }
        return "user" + System.currentTimeMillis();
    }
}
