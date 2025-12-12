package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockInResponse {

    private List<StockInItemResult> items;

    private Integer totalItems;
    private Integer totalQuantity;
}
