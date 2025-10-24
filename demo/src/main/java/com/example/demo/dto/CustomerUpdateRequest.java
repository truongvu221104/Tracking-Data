package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CustomerUpdateRequest {
    @NotBlank @Size(max=128) private String name;
    @Size(max=32)            private String phone;
    @Size(max=128) @Email    private String email;
    @Size(max=255)           private String address;
    private String note;
}