package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)   // ⭐ chỉ tính các field mình chọn
@ToString(exclude = {"images", "categories"})       // tránh toString đệ quy
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include                     // ⭐ equals/hashCode chỉ dựa vào id
    private Long id;

    @EqualsAndHashCode.Include                     // (tuỳ, có thể giữ thêm sku)
    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String name;

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(nullable = false, length = 16, columnDefinition = "NVARCHAR(16)")
    private String unit;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(precision = 18, scale = 2)
    private BigDecimal listPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal markupPercent;

    @Column(precision = 5, scale = 2)
    private BigDecimal minMarginPercent;

    @Column(length = 16)
    private String pricingMode; // FIXED | COST_PLUS

    @Column(length = 16)
    private String status; // ACTIVE | INACTIVE

    @Column(nullable = false)
    private Integer stockMin;

    @Column(nullable = false)
    private Integer stockOnHand;

    private Integer soldCount;

    @Column(precision = 2, scale = 1)
    private BigDecimal ratingAvg;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // ========== Quan hệ ==========

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

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

        if (this.status == null) this.status = "ACTIVE";
        if (this.soldCount == null) this.soldCount = 0;
        if (this.ratingAvg == null) this.ratingAvg = BigDecimal.ZERO;
        if (this.stockOnHand == null) this.stockOnHand = 0;
        if (this.stockMin == null) this.stockMin = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
