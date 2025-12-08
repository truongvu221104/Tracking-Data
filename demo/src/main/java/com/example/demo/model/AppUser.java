package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false, length = 120)
    private String password; // BCrypt

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", length = 32)
    private Set<String> roles;

    @Column(nullable = false)
    private Boolean enabled;           // có cho login không

    @Column(nullable = false)
    private Boolean emailVerified;     // đã xác thực email?

    // Token xác thực email (MVP đơn giản: lưu ngay trên bảng users)
    @Column(length = 64)
    private String verificationToken;

    private Instant verificationExpiry; // hết hạn token

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @OneToOne(mappedBy = "user")
    private Customer customer;
}
