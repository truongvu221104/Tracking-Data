package com.example.demo.repository;

import com.example.demo.model.InventoryLedger;
import com.example.demo.model.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLedgerRepository extends JpaRepository<InventoryLedger, Long> {
    Page<InventoryLedger> findByProduct(Product product, Pageable pageable);
}
