// src/main/java/com/example/demo/dto/StockInItemResult.java
package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockInItemResult {

    private Long productId;
    private String sku;
    private String name;

    private Integer quantityAdded;
    private Integer oldStock;
    private Integer newStock;
}
