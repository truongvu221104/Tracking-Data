package com.example.demo.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAdminDto {
    private Long id;
    private String code;

    private String name;
    private String phone;
    private String note;

    // Thông tin từ AppUser
    private Long userId;
    private String email;
    private Boolean enabled;

    private Instant createdAt;
}
