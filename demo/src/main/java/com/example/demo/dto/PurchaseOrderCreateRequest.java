package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderCreateRequest {
    @NotBlank
    @Size(max = 32)
    private String code;
    @Size(max = 200)
    private String supplierName;
    private LocalDate orderDate;
    @Size(max = 16)
    private String status; // DRAFT|CONFIRMED|CANCELLED
    @NotNull
    @Size(min = 1)
    private List<PurchaseItemReq> items;
    @DecimalMin("0.00")
    private BigDecimal discountAmount;
    @DecimalMin("0.00")
    private BigDecimal taxAmount;
}