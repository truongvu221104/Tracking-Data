package com.example.demo.controller;

import com.example.demo.dto.SalesOrderCreateRequest;
import com.example.demo.model.SalesOrder;
import com.example.demo.service.SalesOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Sales Orders")
public class SalesOrderController {
    private final SalesOrderService service;

    @GetMapping
    public Page<SalesOrder> list(@ParameterObject Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public SalesOrder get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<SalesOrder> create(@RequestBody @Validated SalesOrderCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }
}
