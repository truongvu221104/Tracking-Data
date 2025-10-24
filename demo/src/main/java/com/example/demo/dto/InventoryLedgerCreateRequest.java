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
public class InventoryLedgerCreateRequest {
    @NotNull
    private Long productId;
    @NotBlank
    @Size(max = 16)
    private String refType; // PURCHASE|SALE
    @NotNull
    private Long refId;
    @NotNull
    private Integer qtyDelta; // + nhập, - bán
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal unitCost;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal costDelta;
    private String note;
    @NotNull
    private Instant at;
}
