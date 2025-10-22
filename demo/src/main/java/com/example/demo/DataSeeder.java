package com.example.demo;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Instant;

@Component
public class DataSeeder implements CommandLineRunner {
    private final ProductRepository repo;
    public DataSeeder(ProductRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(Product.builder()
                    .sku("SKU001")
                    .name("Sample Product")
                    .price(BigDecimal.valueOf(99.99))
                    .createdAt(Instant.now())
                    .build());
            System.out.println("✅ Connected to SQL Server & sample product inserted!");
        }
    }
}
