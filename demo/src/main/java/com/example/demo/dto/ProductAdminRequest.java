package com.example.demo.dto;

import java.math.BigDecimal;

// request body khi admin thêm/sửa
public record ProductAdminRequest(
        String sku,
        String name,
        String description,
        String unit,
        BigDecimal price,
        BigDecimal listPrice,
        BigDecimal markupPercent,
        BigDecimal minMarginPercent,
        Integer stockMin,
        Integer stockOnHand,
        String status,                 // ACTIVE / INACTIVE
        java.util.List<Long> categoryIds,
        java.util.List<String> imageUrls
) {}
