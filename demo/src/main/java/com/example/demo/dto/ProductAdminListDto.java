package com.example.demo.dto;

import java.math.BigDecimal;

public record ProductAdminListDto(
        Long id,
        String sku,
        String name,
        Integer stockOnHand,
        Integer stockMin,
        BigDecimal listPrice,
        String status
) {}

