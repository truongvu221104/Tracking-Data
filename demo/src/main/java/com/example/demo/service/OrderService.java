package com.example.demo.service;

import com.example.demo.dto.CheckoutRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.model.*;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CustomerAddressRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartItemRepository cartRepo;
    private final CustomerRepository customerRepo;
    private final CustomerAddressRepository addressRepo;
    private final ProductRepository productRepo;
    private final ShippingService shippingService;

    private Customer getCurrentCustomer() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return customerRepo.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest req) {
        Customer c = getCurrentCustomer();

        if (req.getCartItemIds() == null || req.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn sản phẩm để đặt hàng");
        }

        List<CartItem> items = cartRepo.findAllById(req.getCartItemIds());
        items.removeIf(i -> !i.getCustomer().getId().equals(c.getId()));

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy cart item hợp lệ");
        }

        CustomerAddress address = addressRepo.findById(req.getShippingAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        if (!address.getCustomer().getId().equals(c.getId())) {
            throw new RuntimeException("Không có quyền dùng địa chỉ này");
        }

        // Kiểm tra tồn kho & trừ kho
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : items) {
            Product p = ci.getProduct();
            int requested = ci.getQuantity();
            int available = p.getStockOnHand() != null ? p.getStockOnHand() : 0;

            if (available < requested) {
                throw new RuntimeException(
                        "Sản phẩm " + p.getName() + " chỉ còn " + available + " trong kho");
            }

            // trừ kho
            p.setStockOnHand(available - requested);
            productRepo.save(p);

            BigDecimal line = ci.getUnitPrice()
                    .multiply(BigDecimal.valueOf(requested));
            subtotal = subtotal.add(line);
        }

        BigDecimal shippingFee = shippingService.calculateShippingFee(address);
        BigDecimal total = subtotal.add(shippingFee);

        var now = Instant.now();
        Order order = new Order();
        order.setCode(generateOrderCode());
        order.setCustomer(c);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(total);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        List<OrderItem> orderItems = items.stream()
                .map(ci -> OrderItem.builder()
                        .order(order)
                        .product(ci.getProduct())
                        .productName(ci.getProduct().getName())
                        .unitPrice(ci.getUnitPrice())
                        .quantity(ci.getQuantity())
                        .lineTotal(ci.getUnitPrice()
                                .multiply(BigDecimal.valueOf(ci.getQuantity())))
                        .build()
                ).toList();

        order.setItems(orderItems);

        Order saved = orderRepo.save(order);

        // Xoá các cart item đã đặt hàng
        cartRepo.deleteAll(items);

        return OrderResponse.fromEntity(saved);
    }

    private String generateOrderCode() {
        return "DH" + System.currentTimeMillis();
    }

    public List<OrderResponse> getMyOrders() {
        Customer c = getCurrentCustomer();
        return orderRepo.findByCustomer_Id(c.getId()).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    // Khách tự hủy đơn (nghiệp vụ: chỉ cho hủy khi PENDING)
    @Transactional
    public OrderResponse cancelMyOrder(Long orderId) {
        Customer c = getCurrentCustomer();
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!o.getCustomer().getId().equals(c.getId())) {
            throw new RuntimeException("Không có quyền hủy đơn này");
        }

        if (o.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn ở trạng thái PENDING");
        }

        // Cộng trả tồn kho
        for (OrderItem item : o.getItems()) {
            Product p = item.getProduct();
            int available = p.getStockOnHand() != null ? p.getStockOnHand() : 0;
            p.setStockOnHand(available + item.getQuantity());
            productRepo.save(p);
        }

        o.setStatus(OrderStatus.CANCELLED);
        o.setUpdatedAt(Instant.now());
        orderRepo.save(o);

        return OrderResponse.fromEntity(o);
    }

    // ADMIN phê duyệt đơn
    @Transactional
    public OrderResponse approveOrder(Long orderId) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (o.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt đơn ở trạng thái PENDING");
        }

        o.setStatus(OrderStatus.CONFIRMED);
        o.setUpdatedAt(Instant.now());
        orderRepo.save(o);

        return OrderResponse.fromEntity(o);
    }

    // ADMIN hủy đơn (VD khi thiếu hàng, sai thông tin...)
    @Transactional
    public OrderResponse adminCancelOrder(Long orderId) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (o.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn hàng đã bị hủy trước đó");
        }

        if (o.getStatus() == OrderStatus.SHIPPED
                || o.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy đơn đã giao/hoàn thành");
        }

        // Nếu chưa ship -> trả tồn kho
        if (o.getStatus() == OrderStatus.PENDING
                || o.getStatus() == OrderStatus.CONFIRMED) {

            for (OrderItem item : o.getItems()) {
                Product p = item.getProduct();
                int available = p.getStockOnHand() != null ? p.getStockOnHand() : 0;
                p.setStockOnHand(available + item.getQuantity());
                productRepo.save(p);
            }
        }

        o.setStatus(OrderStatus.CANCELLED);
        o.setUpdatedAt(Instant.now());
        orderRepo.save(o);

        return OrderResponse.fromEntity(o);
    }
}
