package com.example.demo.controller;

import com.example.demo.model.ArLedger;
import com.example.demo.service.ArLedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ar-ledger")
@RequiredArgsConstructor
@Tag(name = "AR Ledger")
public class ArLedgerController {
    private final ArLedgerService service;

    @GetMapping
    public Page<ArLedger> list(@RequestParam(required=false) Long customerId,
                               @ParameterObject Pageable pageable){
        return service.list(customerId, pageable);
    }

    @GetMapping("/{id}")
    public ArLedger get(@PathVariable Long id){ return service.get(id); }
}
