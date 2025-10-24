package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

@Service @RequiredArgsConstructor
public class SalesOrderService {
    private final SalesOrderRepository soRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;

    public Page<SalesOrder> list(Pageable pageable){ return soRepo.findAll(pageable); }

    public SalesOrder get(Long id){
        return soRepo.findById(id).orElseThrow(() -> new RuntimeException("Sales order not found"));
    }

    @Transactional
    public SalesOrder create(SalesOrderCreateRequest req){
        if (soRepo.existsByCode(req.getCode())) throw new IllegalArgumentException("Mã đơn bán đã tồn tại");
        var customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        var so = SalesOrder.builder()
                .code(req.getCode())
                .customer(customer)
                .orderDate(req.getOrderDate())
                .status(req.getStatus())
                .items(new ArrayList<>())
                .createdAt(Instant.now())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (var itReq: req.getItems()){
            var product = productRepo.findById(itReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itReq.getProductId()));
            var base = itReq.getUnitPrice().multiply(BigDecimal.valueOf(itReq.getQty()));
            var discPct = itReq.getDiscountPercent()==null ? BigDecimal.ZERO : itReq.getDiscountPercent();
            var after = base.subtract(base.multiply(discPct).divide(BigDecimal.valueOf(100)));
            subtotal = subtotal.add(after);

            var item = SalesItem.builder()
                    .sales(so)
                    .product(product)
                    .qty(itReq.getQty())
                    .unitPrice(itReq.getUnitPrice())
                    .discount(discPct)
                    .lineTotalAfterDiscount(after)
                    .build();
            so.getItems().add(item);
        }

        var discountAmt = req.getDiscountAmount()==null ? BigDecimal.ZERO : req.getDiscountAmount();
        var taxAmt = req.getTaxAmount()==null ? BigDecimal.ZERO : req.getTaxAmount();
        var grand = subtotal.subtract(discountAmt).add(taxAmt);

        so.setSubtotal(subtotal);
        so.setDiscountAmount(discountAmt);
        so.setTaxAmount(taxAmt);
        so.setGrandTotal(grand);

        return soRepo.save(so);
    }
}
