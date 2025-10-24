package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);

    Page<Product> findByNameContainingIgnoreCase(String q, Pageable pageable);
}
