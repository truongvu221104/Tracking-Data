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
public class PurchaseOrderService {
    private final PurchaseOrderRepository poRepo;
    private final ProductRepository productRepo;

    public Page<PurchaseOrder> list(Pageable pageable){ return poRepo.findAll(pageable); }

    public PurchaseOrder get(Long id){
        return poRepo.findById(id).orElseThrow(() -> new RuntimeException("Purchase order not found"));
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderCreateRequest req){
        if (poRepo.existsByCode(req.getCode())) throw new IllegalArgumentException("Mã phiếu nhập đã tồn tại");
        var po = PurchaseOrder.builder()
                .code(req.getCode())
                .supplierName(req.getSupplierName())
                .orderDate(req.getOrderDate())
                .status(req.getStatus())
                .items(new ArrayList<>())
                .createdAt(Instant.now())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (var itReq: req.getItems()){
            var product = productRepo.findById(itReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itReq.getProductId()));
            var lineTotal = itReq.getUnitPrice().multiply(BigDecimal.valueOf(itReq.getQty()));
            subtotal = subtotal.add(lineTotal);

            var item = PurchaseItem.builder()
                    .purchase(po)
                    .product(product)
                    .qty(itReq.getQty())
                    .unitPrice(itReq.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();
            po.getItems().add(item);
        }

        var discount = req.getDiscountAmount()==null ? BigDecimal.ZERO : req.getDiscountAmount();
        var tax = req.getTaxAmount()==null ? BigDecimal.ZERO : req.getTaxAmount();
        var grand = subtotal.subtract(discount).add(tax);

        po.setSubtotal(subtotal);
        po.setDiscountAmount(discount);
        po.setTaxAmount(tax);
        po.setGrandTotal(grand);

        return poRepo.save(po);
    }
}
