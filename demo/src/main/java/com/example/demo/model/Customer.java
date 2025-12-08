package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã khách (code hiển thị, ASCII, tự gen kiểu CUS0001)
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    // 1 user - 1 customer
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(nullable = false, length = 128, columnDefinition = "NVARCHAR(128)")
    private String name;      // Họ và tên

    @Column(length = 32, columnDefinition = "NVARCHAR(32)")
    private String phone;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // Danh sách địa chỉ giao hàngs
    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<CustomerAddress> addresses = new ArrayList<>();
}




