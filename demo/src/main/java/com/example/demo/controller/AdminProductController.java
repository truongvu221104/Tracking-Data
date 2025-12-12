package com.example.demo.controller;

import com.example.demo.dto.ProductAdminDetailDto;
import com.example.demo.dto.ProductAdminListDto;
import com.example.demo.dto.ProductAdminRequest;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductAdminListDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return productService.adminListProducts(page, size, keyword);
    }

    @GetMapping("/{id}")
    public ProductAdminDetailDto get(@PathVariable Long id) {
        return productService.adminGetProduct(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductAdminDetailDto create(
            @RequestPart("data") ProductAdminRequest req,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        return productService.adminCreateProduct(req, images);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductAdminDetailDto update(
            @PathVariable Long id,
            @RequestPart("data") ProductAdminRequest req,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        return productService.adminUpdateProduct(id, req, images);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.adminDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
