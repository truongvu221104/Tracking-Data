package com.example.demo.dto;

import com.example.demo.model.CustomerAddress;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private String label;
    private boolean isDefault;

    private String countryCode;
    private String provinceCode;
    private String districtCode;
    private String addressLine;

    private Double latitude;
    private Double longitude;

    public static AddressResponse fromEntity(CustomerAddress a) {
        return AddressResponse.builder()
                .id(a.getId())
                .label(a.getLabel())
                .isDefault(Boolean.TRUE.equals(a.getIsDefault()))
                .countryCode(a.getCountryCode())
                .provinceCode(a.getProvinceCode())
                .districtCode(a.getDistrictCode())
                .addressLine(a.getAddressLine())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .build();
    }
}
