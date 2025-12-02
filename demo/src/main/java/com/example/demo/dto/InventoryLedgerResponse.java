package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class InventoryLedgerResponse {
    private Long id;

    private Long productId;
    private String productSku;
    private String productName;

    private String refType;     // PURCHASE | SALE
    private Long refId;
    private Integer qtyDelta;
    private BigDecimal unitCost;
    private BigDecimal costDelta;
    private String note;
    private Instant at;
}