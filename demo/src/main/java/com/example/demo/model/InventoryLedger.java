package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 16, nullable = false)
    private String refType; // PURCHASE | SALE

    @Column(nullable = false)
    private Long refId; // id phiếu nhập/bán

    @Column(nullable = false)
    private Integer qtyDelta; // + nhập, - bán

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal costDelta;

    @Column(columnDefinition = "text")
    private String note;

    @Column(nullable = false)
    private Instant at;
}
