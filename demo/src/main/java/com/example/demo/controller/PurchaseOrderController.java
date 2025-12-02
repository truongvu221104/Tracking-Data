package com.example.demo.controller;

import com.example.demo.dto.PurchaseOrderCreateRequest;
import com.example.demo.model.PurchaseOrder;
import com.example.demo.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Purchase Orders")
public class PurchaseOrderController {
    private final PurchaseOrderService service;

    @GetMapping
    public Page<PurchaseOrder> list(@ParameterObject Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public PurchaseOrder get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> create(@RequestBody @Validated PurchaseOrderCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }
}
