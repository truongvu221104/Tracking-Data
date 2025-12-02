package com.example.demo.controller;

import com.example.demo.dto.InventoryLedgerResponse;
import com.example.demo.model.InventoryLedger;
import com.example.demo.service.InventoryLedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-ledger")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Inventory Ledger")
public class InventoryLedgerController {

    private final InventoryLedgerService service;

    // USER & ADMIN đều xem được
    @GetMapping
    public Page<InventoryLedgerResponse> list(@ParameterObject Pageable pageable) {
        return service.list(pageable);
    }
}
