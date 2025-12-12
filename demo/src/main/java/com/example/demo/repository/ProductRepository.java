package com.example.demo.repository;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);

    Page<Product> findByNameContainingIgnoreCase(String q, Pageable pageable);

    // Lọc theo status
    Page<Product> findByStatus(String status, Pageable pageable);

    // Lọc theo category + status
    Page<Product> findByCategoriesContainsAndStatus(Category category, String status, Pageable pageable);

    // (tuỳ chọn) tìm theo name chứa từ khoá
    Page<Product> findByStatusAndNameContainingIgnoreCase(String status, String keyword, Pageable pageable);

    Page<Product> findByCategoriesContainsAndStatusAndNameContainingIgnoreCase(Category category, String status, String name, Pageable pageable);
}
