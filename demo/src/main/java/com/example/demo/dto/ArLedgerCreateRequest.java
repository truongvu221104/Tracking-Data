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
public class ArLedgerCreateRequest {
    @NotNull
    private Long customerId;
    @NotBlank
    @Size(max = 16)
    private String refType; // SALE|PAYMENT|CREDIT
    @NotNull
    private Long refId;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amountDelta; // SALE +, PAYMENT/CREDIT -
    private String note;
    @NotNull
    private Instant at;
}
