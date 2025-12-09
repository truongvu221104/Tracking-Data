package com.example.demo.dto;

import com.example.demo.model.Order;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String code;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse fromEntity(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .code(o.getCode())
                .status(o.getStatus().name())
                .subtotal(o.getSubtotal())
                .shippingFee(o.getShippingFee())
                .totalAmount(o.getTotalAmount())
                .createdAt(o.getCreatedAt())
                .items(
                        o.getItems().stream()
                                .map(OrderItemResponse::fromEntity)
                                .toList()
                )
                .build();
    }
}
