package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {
    @NotBlank
    @Size(max = 32)
    private String code;
    @NotBlank
    @Size(max = 128)
    private String name;
    @Size(max = 32)
    private String phone;
    private String note;
}