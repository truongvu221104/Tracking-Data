package com.example.demo.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAdminDetailDto {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private String unit;

    private BigDecimal price;
    private BigDecimal listPrice;
    private BigDecimal displayPrice;   // listPrice != null ? listPrice : price

    private Integer stockOnHand;
    private Integer stockMin;
    private String status;

    private BigDecimal ratingAvg;
    private Integer soldCount;

    // Danh mục
    private List<Long> categoryIds;
    private List<String> categoryNames;

    // Ảnh
    private List<String> imageUrls;
}
