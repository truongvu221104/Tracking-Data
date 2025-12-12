package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockInRequest {

    @NotEmpty
    @Valid
    private List<StockInItemRequest> items;
    private String note;
}
