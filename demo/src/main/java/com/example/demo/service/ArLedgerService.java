package com.example.demo.service;

import com.example.demo.model.ArLedger;
import com.example.demo.repository.ArLedgerRepository;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class ArLedgerService {
    private final ArLedgerRepository repo;
    private final CustomerRepository customerRepo;

    public Page<ArLedger> list(Long customerId, Pageable pageable){
        if (customerId == null) return repo.findAll(pageable);
        var customer = customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        return repo.findByCustomer(customer, pageable);
    }

    public ArLedger get(Long id){
        return repo.findById(id).orElseThrow(() -> new RuntimeException("AR ledger not found"));
    }
}
