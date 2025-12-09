// CheckoutRequest: FE gửi khi bấm Đặt hàng
package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private List<Long> cartItemIds;
    private Long shippingAddressId;
}
