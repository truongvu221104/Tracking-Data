package com.example.demo.service;

import com.example.demo.model.InventoryLedger;
import com.example.demo.repository.InventoryLedgerRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class InventoryLedgerService {
    private final InventoryLedgerRepository repo;
    private final ProductRepository productRepo;

    public Page<InventoryLedger> list(Long productId, Pageable pageable){
        if (productId == null) return repo.findAll(pageable);
        var product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        return repo.findByProduct(product, pageable);
    }

    public InventoryLedger get(Long id){
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Inventory ledger not found"));
    }
}
