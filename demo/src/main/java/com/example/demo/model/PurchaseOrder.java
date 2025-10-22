package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(length = 200)
    private String supplierName;

    private LocalDate orderDate;

    @Column(length = 16)
    private String status; // DRAFT | CONFIRMED | CANCELLED

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal grandTotal;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseItem> items;

    private Instant createdAt;
}
