package com.example.demo.service;

import com.example.demo.model.CustomerAddress;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingService {

    // TODO: sau đọc từ application.yml
    private static final double WAREHOUSE_LAT = 21.0278;
    private static final double WAREHOUSE_LNG = 105.8342;

    public BigDecimal calculateShippingFee(CustomerAddress address) {
        if (address.getLatitude() == null || address.getLongitude() == null) {
            throw new IllegalArgumentException("Địa chỉ chưa có toạ độ, không thể tính phí ship");
        }

        double distanceKm = distanceKm(
                WAREHOUSE_LAT,
                WAREHOUSE_LNG,
                address.getLatitude(),
                address.getLongitude()
        );

        return feeByDistance(distanceKm);
    }

    // Haversine
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Rule phí ship
    private BigDecimal feeByDistance(double d) {
        if (d <= 5)  return new BigDecimal("20000");
        if (d <= 10) return new BigDecimal("30000");
        if (d <= 30) return new BigDecimal("40000");
        return new BigDecimal("50000");
    }
}
