package com.example.demo.service;

import com.example.demo.dto.ProductAdminListDto;
import com.example.demo.dto.ProductAdminRequest;
import com.example.demo.dto.ProductCardDto;
import com.example.demo.dto.ProductDetailDto;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductCardDto> listProducts(Integer page, Integer size, Long categoryId, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products;

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            if (keyword != null && !keyword.isBlank()) {
                // nếu muốn kết hợp keyword + category thì phải custom query,
                // còn tạm thời chỉ lọc theo category
            }
            products = productRepository.findByCategoriesContainsAndStatus(category, "ACTIVE", pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            products = productRepository.findByStatusAndNameContainingIgnoreCase("ACTIVE", keyword, pageable);
        } else {
            products = productRepository.findByStatus("ACTIVE", pageable);
        }

        return products.map(this::toCardDto);
    }

    public ProductDetailDto getProductDetail(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDetailDto(p);
    }

    private ProductCardDto toCardDto(Product p) {
        String coverImage = p.getImages().stream()
                .sorted((a, b) -> {
                    // ưu tiên isCover, sau đó tới sortOrder, rồi id
                    int c1 = Boolean.TRUE.equals(a.getIsCover()) ? 0 : 1;
                    int c2 = Boolean.TRUE.equals(b.getIsCover()) ? 0 : 1;
                    if (c1 != c2) return Integer.compare(c1, c2);
                    Integer s1 = a.getSortOrder() != null ? a.getSortOrder() : 9999;
                    Integer s2 = b.getSortOrder() != null ? b.getSortOrder() : 9999;
                    int sortCompare = s1.compareTo(s2);
                    if (sortCompare != 0) return sortCompare;
                    return a.getId().compareTo(b.getId());
                })
                .map(img -> img.getUrl())
                .findFirst()
                .orElse(null);

        return new ProductCardDto(
                p.getId(),
                p.getName(),
                coverImage,
                p.getListPrice() != null ? p.getListPrice() : p.getPrice(),
                p.getRatingAvg(),
                p.getSoldCount()
        );
    }

    private ProductDetailDto toDetailDto(Product p) {
        List<String> imageUrls = p.getImages().stream()
                .sorted((a, b) -> {
                    Integer s1 = a.getSortOrder() != null ? a.getSortOrder() : 9999;
                    Integer s2 = b.getSortOrder() != null ? b.getSortOrder() : 9999;
                    return s1.compareTo(s2);
                })
                .map(img -> img.getUrl())
                .toList();

        List<String> categories = p.getCategories().stream()
                .map(cat -> cat.getName())
                .toList();

        return new ProductDetailDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getUnit(),
                p.getPrice(),
                p.getListPrice() != null ? p.getListPrice() : p.getPrice(),
                p.getStockOnHand(),
                p.getRatingAvg(),
                p.getSoldCount(),
                imageUrls,
                categories
        );
    }

    public Page<ProductAdminListDto> adminListProducts(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products;

        if (keyword != null && !keyword.isBlank()) {
            products = productRepository.findByStatusAndNameContainingIgnoreCase("ACTIVE", keyword, pageable);
            // hoặc tìm tất cả bất kể status:
            // products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(p -> new ProductAdminListDto(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getStockOnHand(),
                p.getStockMin(),
                p.getListPrice() != null ? p.getListPrice() : p.getPrice(),
                p.getStatus()
        ));
    }

    public ProductDetailDto adminCreateProduct(ProductAdminRequest req) {
        Product p = new Product();
        applyAdminRequestToProduct(p, req);
        Product saved = productRepository.save(p);
        return toDetailDto(saved);
    }

    public ProductDetailDto adminUpdateProduct(Long id, ProductAdminRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Xóa ảnh cũ (vì orphanRemoval = true)
        p.getImages().clear();
        p.getCategories().clear();

        applyAdminRequestToProduct(p, req);

        Product saved = productRepository.save(p);
        return toDetailDto(saved);
    }

    public void adminDeleteProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // soft delete
        p.setStatus("INACTIVE");
        productRepository.save(p);
    }

    private void applyAdminRequestToProduct(Product p, ProductAdminRequest req) {
        p.setSku(req.sku());
        p.setName(req.name());
        p.setDescription(req.description());
        p.setUnit(req.unit());
        p.setPrice(req.price());
        p.setListPrice(req.listPrice());
        p.setMarkupPercent(req.markupPercent());
        p.setMinMarginPercent(req.minMarginPercent());
        p.setStockMin(req.stockMin() != null ? req.stockMin() : 0);
        p.setStockOnHand(req.stockOnHand() != null ? req.stockOnHand() : 0);

        if (req.status() != null && !req.status().isBlank()) {
            p.setStatus(req.status());
        } else if (p.getStatus() == null) {
            p.setStatus("ACTIVE");
        }

        // categories
        if (req.categoryIds() != null && !req.categoryIds().isEmpty()) {
            var cats = new java.util.HashSet<>(
                    categoryRepository.findAllById(req.categoryIds())
            );
            p.setCategories(cats);
        }

        // images
        if (req.imageUrls() != null && !req.imageUrls().isEmpty()) {
            int i = 0;
            for (String url : req.imageUrls()) {
                ProductImage img = ProductImage.builder()
                        .product(p)
                        .url(url)
                        .sortOrder(i)
                        .isCover(i == 0)
                        .build();
                p.getImages().add(img);
                i++;
            }
        }
    }

}
