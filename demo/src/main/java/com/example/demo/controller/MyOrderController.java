package com.example.demo.controller;

import com.example.demo.dto.CheckoutRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/orders")
@RequiredArgsConstructor
public class MyOrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> listMyOrders() {
        return orderService.getMyOrders();
    }

    @PostMapping("/checkout")
    public OrderResponse checkout(@RequestBody CheckoutRequest req) {
        return orderService.checkout(req);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelMyOrder(@PathVariable Long id) {
        return orderService.cancelMyOrder(id);
    }
}
