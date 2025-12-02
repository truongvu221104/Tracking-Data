package com.example.demo.service;

import com.example.demo.dto.InventoryLedgerResponse;
import com.example.demo.model.InventoryLedger;
import com.example.demo.repository.InventoryLedgerRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class InventoryLedgerService {
    private final InventoryLedgerRepository repo;

    public Page<InventoryLedgerResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(this::toDto);
    }

    private InventoryLedgerResponse toDto(InventoryLedger e) {
        return InventoryLedgerResponse.builder()
                .id(e.getId())
                .productId(e.getProduct().getId())
                .productSku(e.getProduct().getSku())
                .productName(e.getProduct().getName())
                .refType(e.getRefType())
                .refId(e.getRefId())
                .qtyDelta(e.getQtyDelta())
                .unitCost(e.getUnitCost())
                .costDelta(e.getCostDelta())
                .note(e.getNote())
                .at(e.getAt())
                .build();
    }
}
