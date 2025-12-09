package com.example.demo.controller;

import com.example.demo.dto.OrderResponse;
import com.example.demo.model.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepo;

    @GetMapping
    public List<OrderResponse> listAll(
            @RequestParam(required = false) String status
    ) {
        if (status == null || status.isBlank()) {
            return orderRepo.findAll().stream()
                    .map(OrderResponse::fromEntity)
                    .toList();
        } else {
            OrderStatus st = OrderStatus.valueOf(status);
            return orderRepo.findByStatus(st).stream()
                    .map(OrderResponse::fromEntity)
                    .toList();
        }
    }

    @PostMapping("/{id}/approve")
    public OrderResponse approve(@PathVariable Long id) {
        return orderService.approveOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse adminCancel(@PathVariable Long id) {
        return orderService.adminCancelOrder(id);
    }
}
