package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductCardDto> listProducts(Integer page, Integer size, Long categoryId, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        String kw = (keyword != null) ? keyword.trim() : null;
        boolean hasKeyword = kw != null && !kw.isBlank();

        Page<Product> products;

        if (categoryId != null && hasKeyword) {
            Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));

            products = productRepository.findByCategoriesContainsAndStatusAndNameContainingIgnoreCase(category, "ACTIVE", kw, pageable);

        } else if (categoryId != null) {
            // 👉 chỉ lọc theo category
            Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
            products = productRepository.findByCategoriesContainsAndStatus(category, "ACTIVE", pageable);

        } else if (hasKeyword) {
            products = productRepository.findByStatusAndNameContainingIgnoreCase("ACTIVE", kw, pageable);
        } else {
            products = productRepository.findByStatus("ACTIVE", pageable);
        }
        return products.map(this::toCardDto);
    }


    public ProductDetailDto getProductDetail(Long id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return toDetailDto(p);
    }

    private ProductCardDto toCardDto(Product p) {
        String coverImage = p.getImages().stream().sorted((a, b) -> {
            // ưu tiên isCover, sau đó tới sortOrder, rồi id
            int c1 = Boolean.TRUE.equals(a.getIsCover()) ? 0 : 1;
            int c2 = Boolean.TRUE.equals(b.getIsCover()) ? 0 : 1;
            if (c1 != c2) return Integer.compare(c1, c2);
            Integer s1 = a.getSortOrder() != null ? a.getSortOrder() : 9999;
            Integer s2 = b.getSortOrder() != null ? b.getSortOrder() : 9999;
            int sortCompare = s1.compareTo(s2);
            if (sortCompare != 0) return sortCompare;
            return a.getId().compareTo(b.getId());
        }).map(img -> img.getUrl()).findFirst().orElse(null);

        return new ProductCardDto(p.getId(), p.getName(), coverImage, p.getListPrice() != null ? p.getListPrice() : p.getPrice(), p.getRatingAvg(), p.getSoldCount());
    }

    private ProductDetailDto toDetailDto(Product p) {
        List<String> imageUrls = p.getImages().stream().sorted((a, b) -> {
            Integer s1 = a.getSortOrder() != null ? a.getSortOrder() : 9999;
            Integer s2 = b.getSortOrder() != null ? b.getSortOrder() : 9999;
            return s1.compareTo(s2);
        }).map(img -> img.getUrl()).toList();

        List<String> categories = p.getCategories().stream().map(cat -> cat.getName()).toList();

        return new ProductDetailDto(p.getId(), p.getName(), p.getDescription(), p.getUnit(), p.getPrice(), p.getListPrice() != null ? p.getListPrice() : p.getPrice(), p.getStockOnHand(), p.getRatingAvg(), p.getSoldCount(), imageUrls, categories);
    }


    // ========= ADMIN: LIST =========

    private final FileStorageService fileStorageService;

    public Page<ProductAdminListDto> adminListProducts(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Product> products;
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            products = productRepository.findByNameContainingIgnoreCase(kw, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(p -> {
            BigDecimal displayPrice =
                    p.getListPrice() != null ? p.getListPrice() : p.getPrice();
            return ProductAdminListDto.builder()
                    .id(p.getId())
                    .sku(p.getSku())
                    .name(p.getName())
                    .stockOnHand(p.getStockOnHand())
                    .stockMin(p.getStockMin())
                    .displayPrice(displayPrice)
                    .status(p.getStatus())
                    .build();
        });
    }

    public ProductAdminDetailDto adminGetProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toAdminDetailDto(p);
    }

    private ProductAdminDetailDto toAdminDetailDto(Product p) {
        List<String> imageUrls = (p.getImages() == null)
                ? List.of()
                : p.getImages().stream()
                .sorted(Comparator.comparing(
                        img -> img.getSortOrder() == null ? 0 : img.getSortOrder()))
                .map(ProductImage::getUrl)
                .toList();

        List<Long> categoryIds = (p.getCategories() == null)
                ? List.of()
                : p.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        List<String> categoryNames = (p.getCategories() == null)
                ? List.of()
                : p.getCategories().stream()
                .map(Category::getName)
                .toList();

        BigDecimal displayPrice =
                p.getListPrice() != null ? p.getListPrice() : p.getPrice();

        return ProductAdminDetailDto.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .description(p.getDescription())
                .unit(p.getUnit())
                .price(p.getPrice())
                .listPrice(p.getListPrice())
                .displayPrice(displayPrice)
                .stockOnHand(p.getStockOnHand())
                .stockMin(p.getStockMin())
                .status(p.getStatus())
                .ratingAvg(p.getRatingAvg())
                .soldCount(p.getSoldCount())
                .categoryIds(categoryIds)
                .categoryNames(categoryNames)
                .imageUrls(imageUrls)
                .build();
    }

    @Transactional
    public ProductAdminDetailDto adminCreateProduct(ProductAdminRequest req,
                                                    MultipartFile[] images) {
        Product p = new Product();
        applyAdminRequestToProduct(p, req);
        Product saved = productRepository.save(p);
        handleReplaceImages(saved, images);
        Product saved2 = productRepository.save(saved);
        return toAdminDetailDto(saved2);
    }

    @Transactional
    public ProductAdminDetailDto adminUpdateProduct(Long id,
                                                    ProductAdminRequest req,
                                                    MultipartFile[] images) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        p.getCategories().clear();
        applyAdminRequestToProduct(p, req);

        if (images != null) {
            p.getImages().clear();
            handleReplaceImages(p, images);
        }

        Product saved = productRepository.save(p);
        return toAdminDetailDto(saved);
    }

    @Transactional
    public void adminDeleteProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        p.setStatus("INACTIVE");
        productRepository.save(p);
    }

    private void applyAdminRequestToProduct(Product p, ProductAdminRequest req) {
        p.setSku(req.getSku());
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setUnit(req.getUnit());
        p.setPrice(req.getPrice());
        p.setListPrice(req.getListPrice());
        p.setMarkupPercent(req.getMarkupPercent());
        p.setMinMarginPercent(req.getMinMarginPercent());

        p.setStockMin(req.getStockMin() != null ? req.getStockMin() : 0);
        p.setStockOnHand(req.getStockOnHand() != null ? req.getStockOnHand() : 0);

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            p.setStatus(req.getStatus());
        } else if (p.getStatus() == null) {
            p.setStatus("ACTIVE");
        }

        if (req.getCategoryIds() != null && !req.getCategoryIds().isEmpty()) {
            var cats = new HashSet<>(categoryRepository.findAllById(req.getCategoryIds()));
            p.setCategories(cats);
        }
    }

    private void handleReplaceImages(Product p, MultipartFile[] images) {
        if (images == null || images.length == 0) return;

        int sort = 0;
        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;

            String url = fileStorageService.storeProductImage(p.getId(), file);

            ProductImage img = ProductImage.builder()
                    .product(p)
                    .url(url)
                    .sortOrder(sort)
                    .isCover(sort == 0)
                    .build();

            p.getImages().add(img);
            sort++;
        }
    }
}

