// src/main/java/com/example/demo/service/InventoryService.java
package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    @Transactional
    public StockInResponse stockIn(StockInRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm nhập kho không được rỗng");
        }

        // Gom sản phẩm theo ID để tránh query lẻ tẻ
        Set<Long> ids = new HashSet<>();
        for (StockInItemRequest item : req.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải > 0");
            }
            ids.add(item.getProductId());
        }

        Map<Long, Product> productMap = productRepository.findAllById(ids)
                .stream()
                .collect(HashMap::new, (m, p) -> m.put(p.getId(), p), HashMap::putAll);

        List<StockInItemResult> results = new ArrayList<>();
        int totalQuantity = 0;

        for (StockInItemRequest item : req.getItems()) {
            Product p = productMap.get(item.getProductId());
            if (p == null) {
                throw new RuntimeException("Không tìm thấy sản phẩm ID = " + item.getProductId());
            }

            int oldStock = p.getStockOnHand() != null ? p.getStockOnHand() : 0;
            int qty = item.getQuantity();
            int newStock = oldStock + qty;

            p.setStockOnHand(newStock);
            totalQuantity += qty;

            results.add(
                    StockInItemResult.builder()
                            .productId(p.getId())
                            .sku(p.getSku())
                            .name(p.getName())
                            .quantityAdded(qty)
                            .oldStock(oldStock)
                            .newStock(newStock)
                            .build()
            );
        }

        // Lưu lại toàn bộ thay đổi
        productRepository.saveAll(productMap.values());

        return StockInResponse.builder()
                .items(results)
                .totalItems(results.size())
                .totalQuantity(totalQuantity)
                .build();
    }
}
