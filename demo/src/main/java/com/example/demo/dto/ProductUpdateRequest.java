package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    @NotBlank
    @Size(max = 200)
    private String name;
    @Size(max = 255)
    private String description;
    @NotBlank
    @Size(max = 16)
    private String unit;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal listPrice;
    @DecimalMin("0.00")
    private BigDecimal markupPercent;
    @DecimalMin("0.00")
    private BigDecimal minMarginPercent;
    @Size(max = 16)
    private String pricingMode;
    @Size(max = 16)
    private String status;
    @NotNull
    @Min(0)
    private Integer stockMin;
}