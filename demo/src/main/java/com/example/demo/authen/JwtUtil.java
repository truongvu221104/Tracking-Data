package com.example.demo.authen;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class
JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        // secret >= 32 bytes cho HS256
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();

        var builder = Jwts.builder();

        // Cách 1: set claims từng cái để không reset các registered claims:
        if (claims != null) {
            claims.forEach(builder::claim); // roles, uid, emailVerified, ...
        }

        return builder
                .subject(subject)                  // username
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }


    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
