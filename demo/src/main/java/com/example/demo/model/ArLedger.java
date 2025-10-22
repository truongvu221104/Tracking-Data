package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ar_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(length = 16, nullable = false)
    private String refType; // SALE | PAYMENT | CREDIT

    @Column(nullable = false)
    private Long refId; // id SalesOrder hoặc Payment

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amountDelta; // SALE +, PAYMENT/CREDIT -

    @Column(columnDefinition = "text")
    private String note;

    @Column(nullable = false)
    private Instant at;
}
