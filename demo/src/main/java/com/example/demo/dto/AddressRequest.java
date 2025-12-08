package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @Size(max = 64)
    private String label;          // Nhãn: Nhà riêng, Cơ quan...

    @Size(max = 8)
    private String countryCode;    // "VN" (em có thể fix cứng ở FE cũng được)

    @NotBlank
    @Size(max = 16)
    private String provinceCode;   // mã tỉnh theo API

    @NotBlank
    @Size(max = 16)
    private String districtCode;   // mã quận theo API

    @NotBlank
    @Size(max = 255)
    private String addressLine;    // "Số 10, ngõ X, đường Y"

    private Boolean isDefault;

    private Double latitude;
    private Double longitude;
}