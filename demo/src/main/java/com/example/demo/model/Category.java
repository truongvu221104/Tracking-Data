package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String name;     // "Đồ uống", "Mì tôm", "Khuyến mãi hot"

    @Column(length = 160)
    private String slug;     // "do-uong", "mi-tom" (optional cho SEO / URL)

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<Product> products = new HashSet<>();
}
