package com.example.demo.dto;

import java.math.BigDecimal;

public record ProductCardDto(
        Long id,
        String name,
        String coverImageUrl,
        BigDecimal listPrice,
        BigDecimal ratingAvg,
        Integer soldCount
) {}
