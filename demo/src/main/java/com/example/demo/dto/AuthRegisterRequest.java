package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;
}