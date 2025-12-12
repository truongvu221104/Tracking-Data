// src/main/java/com/example/demo/controller/AdminInventoryController.java
package com.example.demo.controller;

import com.example.demo.dto.StockInRequest;
import com.example.demo.dto.StockInResponse;
import com.example.demo.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/stock-in")
    public StockInResponse stockIn(@RequestBody @Valid StockInRequest req) {
        return inventoryService.stockIn(req);
    }
}
