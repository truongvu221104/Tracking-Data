package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service @RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repo;

    public Page<Customer> search(String q, Pageable pageable){
        var p = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending())
                : pageable;
        return (q==null || q.isBlank()) ? repo.findAll(p) : repo.findByNameContainingIgnoreCase(q, p);
    }

    public Customer get(Long id){
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Customer create(CustomerCreateRequest req){
        if (repo.existsByCode(req.getCode())) throw new IllegalArgumentException("Mã khách hàng đã tồn tại");
        var now = Instant.now();
        var c = Customer.builder()
                .code(req.getCode()).name(req.getName())
                .phone(req.getPhone()).email(req.getEmail())
                .address(req.getAddress()).note(req.getNote())
                .createdAt(now).updatedAt(now)
                .build();
        return repo.save(c);
    }

    public Customer update(Long id, CustomerUpdateRequest req){
        var c = get(id);
        c.setName(req.getName());
        c.setPhone(req.getPhone());
        c.setEmail(req.getEmail());
        c.setAddress(req.getAddress());
        c.setNote(req.getNote());
        c.setUpdatedAt(Instant.now());
        return repo.save(c);
    }

    public void delete(Long id){
        if (!repo.existsById(id)) throw new RuntimeException("Customer not found");
        repo.deleteById(id);
    }
}
