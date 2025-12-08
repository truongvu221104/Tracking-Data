package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SKU thường là mã kỹ thuật (ASCII) nên dùng VARCHAR là đủ
    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    // Tên sản phẩm tiếng Việt
    @Column(nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String name;

    // Mô tả tiếng Việt
    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String description;

    // Đơn vị tính (HỘP, CHIẾC, BỊCH...) nên để NVARCHAR
    @Column(nullable = false, length = 16, columnDefinition = "NVARCHAR(16)")
    private String unit;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price; // giá cơ sở

    @Column(precision = 18, scale = 2)
    private BigDecimal listPrice; // giá bán hiện hành

    @Column(precision = 5, scale = 2)
    private BigDecimal markupPercent; // % lãi nếu cost-plus

    @Column(precision = 5, scale = 2)
    private BigDecimal minMarginPercent;

    // Các field logic nội bộ, có thể để tiếng Anh / VARCHAR
    @Column(length = 16)
    private String pricingMode; // FIXED | COST_PLUS

    @Column(length = 16)
    private String status; // ACTIVE | INACTIVE

    @Column(nullable = false)
    private Integer stockMin;

    // Số lượng tồn hiện tại trong kho (đơn giản: 1 kho duy nhất)
    @Column(nullable = false)
    private Integer stockOnHand;

    // Đã bán bao nhiêu (để hiển thị "Đã bán 123")
    private Integer soldCount;

    // Điểm đánh giá trung bình (0.0 - 5.0)
    @Column(precision = 2, scale = 1)
    private BigDecimal ratingAvg;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // ========== Quan hệ ==========

    // 1 product - N images
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    // 1 product - N categories
    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    // ========== Hook thời gian ==========

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.soldCount == null) {
            this.soldCount = 0;
        }
        if (this.ratingAvg == null) {
            this.ratingAvg = BigDecimal.ZERO;
        }
        if (this.stockOnHand == null) {
            this.stockOnHand = 0;
        }
        if (this.stockMin == null) {
            this.stockMin = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
