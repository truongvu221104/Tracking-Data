package com.example.demo.dto;

import com.example.demo.model.Customer;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {

    private Long id;
    private String code;
    private String name;
    private String phone;
    private String note;

    private String email;  // lấy từ AppUser cho tiện FE hiển thị

    public static CustomerProfileResponse fromEntity(Customer c) {
        return CustomerProfileResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .phone(c.getPhone())
                .note(c.getNote())
                .email(c.getUser() != null ? c.getUser().getEmail() : null)
                .build();
    }
}
