package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileRequest {

    @NotBlank
    @Size(max = 128)
    private String name;       // Họ và tên khách

    @NotBlank
    @Size(max = 32)
    private String phone;      // Số điện thoại

    @Size(max = 1024)
    private String note;       // Ghi chú thêm (tuỳ chọn)
}
