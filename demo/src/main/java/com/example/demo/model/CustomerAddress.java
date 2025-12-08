package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "customer_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chủ sở hữu địa chỉ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    // Nhãn địa chỉ: "Nhà riêng", "Cơ quan", ...
    @Column(length = 64, columnDefinition = "NVARCHAR(64)")
    private String label;

    // Địa chỉ có là mặc định không
    @Column(nullable = false)
    private Boolean isDefault;

    // Mã khu vực theo API (dù là số vẫn nên lưu String)
    @Column(length = 8)
    private String countryCode;   // "VN"

    @Column(length = 16)
    private String provinceCode;  // "01", "79", ...

    @Column(length = 16)
    private String districtCode;  // "001", "760", ...

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String addressLine;   // "Số 10, ngõ X, đường Y"

    // (Tuỳ chọn) Toạ độ map để chơi geocoding sau này
    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
}
