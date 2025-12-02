package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "social_accounts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GOOGLE, FACEBOOK,...
    @Column(nullable = false, length = 32)
    private String provider;

    // sub (Google) hoặc id (Facebook)
    @Column(name = "provider_user_id", nullable = false, length = 128)
    private String providerUserId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}