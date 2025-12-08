package com.example.demo.controller;

import com.example.demo.dto.ProductAdminListDto;
import com.example.demo.dto.ProductAdminRequest;
import com.example.demo.dto.ProductDetailDto;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    // List cho admin (table)
    @GetMapping
    public Page<ProductAdminListDto> list(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        return productService.adminListProducts(page, size, keyword);
    }

    // Xem chi tiết (dùng luôn ProductDetailDto)
    @GetMapping("/{id}")
    public ProductDetailDto get(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    // Thêm mới
    @PostMapping
    public ProductDetailDto create(@RequestBody ProductAdminRequest req) {
        return productService.adminCreateProduct(req);
    }

    // Sửa
    @PutMapping("/{id}")
    public ProductDetailDto update(@PathVariable Long id,
                                   @RequestBody ProductAdminRequest req) {
        return productService.adminUpdateProduct(id, req);
    }

    // Xóa (soft delete)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.adminDeleteProduct(id);
    }
}

