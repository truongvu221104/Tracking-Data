package com.example.demo.controller;

import com.example.demo.dto.CustomerAdminDto;
import com.example.demo.dto.CustomerAdminUpdateRequest;
import com.example.demo.service.CustomerAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CustomerAdminController {

    private final CustomerAdminService customerAdminService;

    @GetMapping
    public List<CustomerAdminDto> getAll() {
        return customerAdminService.getAll();
    }

    @GetMapping("/{id}")
    public CustomerAdminDto getById(@PathVariable Long id) {
        return customerAdminService.getById(id);
    }

    @PutMapping("/{id}")
    public CustomerAdminDto update(
            @PathVariable Long id,
            @RequestBody CustomerAdminUpdateRequest req
    ) {
        return customerAdminService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
