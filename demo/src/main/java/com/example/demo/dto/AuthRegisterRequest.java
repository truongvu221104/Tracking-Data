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
    @Email
    @Size(max = 128)
    private String email;
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;
    @NotBlank @Size(min=6, max=64)
    private String confirmPassword;
    @AssertTrue(message = "Mật khẩu xác nhận không khớp")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
