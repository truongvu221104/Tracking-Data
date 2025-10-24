package com.example.demo.controller;

import com.example.demo.model.InventoryLedger;
import com.example.demo.service.InventoryLedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-ledger")
@RequiredArgsConstructor
@Tag(name = "Inventory Ledger")
public class InventoryLedgerController {
    private final InventoryLedgerService service;

    @GetMapping
    public Page<InventoryLedger> list(@RequestParam(required=false) Long productId,
                                      @ParameterObject Pageable pageable){
        return service.list(productId, pageable);
    }

    @GetMapping("/{id}")
    public InventoryLedger get(@PathVariable Long id){ return service.get(id); }
}
