package com.example.demo.model;

public enum OrderStatus {
    PENDING,     // khách vừa đặt, chờ duyệt
    CONFIRMED,   // admin duyệt, đang chuẩn bị/giao
    SHIPPED,     // đã giao cho đơn vị vận chuyển (tạm chưa dùng)
    COMPLETED,   // hoàn thành (tạm chưa dùng)
    CANCELLED    // đã hủy
}
