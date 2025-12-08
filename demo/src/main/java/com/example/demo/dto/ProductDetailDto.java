package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailDto(
        Long id,
        String name,
        String description,
        String unit,
        BigDecimal price,
        BigDecimal listPrice,
        Integer stockOnHand,
        BigDecimal ratingAvg,
        Integer soldCount,
        List<String> imageUrls,
        List<String> categories
) {}
