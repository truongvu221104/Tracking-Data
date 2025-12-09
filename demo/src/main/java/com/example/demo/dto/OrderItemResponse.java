// OrderItemResponse
package com.example.demo.dto;

import com.example.demo.model.OrderItem;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;

    public static OrderItemResponse fromEntity(OrderItem i) {
        return OrderItemResponse.builder()
                .productId(i.getProduct().getId())
                .productName(i.getProductName())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .lineTotal(i.getLineTotal())
                .build();
    }
}
