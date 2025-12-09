package com.example.demo.service;

import com.example.demo.dto.CartItemRequest;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.model.CartItem;
import com.example.demo.model.Customer;
import com.example.demo.model.Product;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;

    private Customer getCurrentCustomer() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // hoặc email tuỳ em config

        return customerRepo.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));
    }

    public List<CartItemResponse> getMyCart() {
        Customer c = getCurrentCustomer();
        return cartRepo.findByCustomer(c).stream()
                .map(CartItemResponse::fromEntity)
                .toList();
    }

    public CartItemResponse addToCart(CartItemRequest req) {
        Customer c = getCurrentCustomer();
        Product p = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        var now = Instant.now();

        // Nếu đã có trong giỏ -> cộng thêm số lượng
        var existing = cartRepo.findByCustomer(c).stream()
                .filter(i -> i.getProduct().getId().equals(p.getId()))
                .findFirst()
                .orElse(null);

        CartItem item;
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            existing.setUpdatedAt(now);
            item = existing;
        } else {
            item = CartItem.builder()
                    .customer(c)
                    .product(p)
                    .quantity(req.getQuantity())
                    .unitPrice(p.getListPrice()) // snapshot giá hiện tại
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

        CartItem saved = cartRepo.save(item);
        return CartItemResponse.fromEntity(saved);
    }

    public void updateQuantity(Long cartItemId, CartItemRequest req) {
        Customer c = getCurrentCustomer();
        CartItem item = cartRepo.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCustomer().getId().equals(c.getId())) {
            throw new RuntimeException("Không có quyền sửa cart item này");
        }

        item.setQuantity(req.getQuantity());
        item.setUpdatedAt(Instant.now());
        cartRepo.save(item);
    }

    public void removeItem(Long cartItemId) {
        Customer c = getCurrentCustomer();
        CartItem item = cartRepo.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCustomer().getId().equals(c.getId())) {
            throw new RuntimeException("Không có quyền xoá cart item này");
        }

        cartRepo.delete(item);
    }
}
