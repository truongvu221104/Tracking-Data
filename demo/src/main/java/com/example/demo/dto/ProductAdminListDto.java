package com.example.demo.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAdminListDto {

    private Long id;
    private String sku;
    private String name;
    private Integer stockOnHand;
    private Integer stockMin;
    private BigDecimal displayPrice;
    private String status;
}
