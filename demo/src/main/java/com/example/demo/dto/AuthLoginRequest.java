package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
