package com.example.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAdminUpdateRequest {
    private String name;
    private String phone;
    private String note;
    private Boolean enabled; // true = active, false = khóa
}
