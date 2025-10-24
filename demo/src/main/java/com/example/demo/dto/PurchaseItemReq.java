package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemReq {
    @NotNull
    private Long productId;
    @NotNull
    @Min(1)
    private Integer qty;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal unitPrice;
}