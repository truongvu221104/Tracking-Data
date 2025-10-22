package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 16)
    private String unit;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price; // giá cơ sở

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal listPrice; // giá bán hiện hành

    @Column(precision = 5, scale = 2)
    private BigDecimal markupPercent; // % lãi nếu cost-plus

    @Column(precision = 5, scale = 2)
    private BigDecimal minMarginPercent;

    @Column(length = 16)
    private String pricingMode; // FIXED | COST_PLUS

    @Column(length = 16)
    private String status; // ACTIVE | INACTIVE

    @Column(nullable = false)
    private Integer stockMin;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
}
