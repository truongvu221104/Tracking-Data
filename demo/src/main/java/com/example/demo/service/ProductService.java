package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service @RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;

    public Page<Product> search(String q, Pageable pageable){
        var p = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending())
                : pageable;
        return (q==null || q.isBlank()) ? repo.findAll(p) : repo.findByNameContainingIgnoreCase(q, p);
    }

    public Product get(Long id){
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product create(ProductCreateRequest req){
        if (repo.existsBySku(req.getSku())) throw new IllegalArgumentException("Mã sản phẩm đã tồn tại");
        var now = Instant.now();
        var p = Product.builder()
                .sku(req.getSku()).name(req.getName()).description(req.getDescription())
                .unit(req.getUnit()).price(req.getPrice()).listPrice(req.getListPrice())
                .markupPercent(req.getMarkupPercent()).minMarginPercent(req.getMinMarginPercent())
                .pricingMode(req.getPricingMode()).status(req.getStatus())
                .stockMin(req.getStockMin())
                .createdAt(now).updatedAt(now)
                .build();
        return repo.save(p);
    }

    public Product update(Long id, ProductUpdateRequest req){
        var p = get(id);
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setUnit(req.getUnit());
        p.setPrice(req.getPrice());
        p.setListPrice(req.getListPrice());
        p.setMarkupPercent(req.getMarkupPercent());
        p.setMinMarginPercent(req.getMinMarginPercent());
        p.setPricingMode(req.getPricingMode());
        p.setStatus(req.getStatus());
        p.setStockMin(req.getStockMin());
        p.setUpdatedAt(Instant.now());
        return repo.save(p);
    }

    public void delete(Long id){
        if (!repo.existsById(id)) throw new RuntimeException("Product not found");
        repo.deleteById(id);
    }
}
