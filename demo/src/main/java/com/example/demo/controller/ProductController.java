package com.example.demo.controller;

import com.example.demo.dto.ProductCardDto;
import com.example.demo.dto.ProductDetailDto;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // tạm cho FE gọi
public class ProductController {

    private final ProductService productService;

    // Danh sách sản phẩm (giống trang chủ / trang danh mục Shopee)
    @GetMapping
    public Page<ProductCardDto> listProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword
    ) {
        return productService.listProducts(page, size, categoryId, keyword);
    }

    // Chi tiết sản phẩm
    @GetMapping("/{id}")
    public ProductDetailDto getDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }
}
