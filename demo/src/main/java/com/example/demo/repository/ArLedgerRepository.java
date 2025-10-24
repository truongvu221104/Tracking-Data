package com.example.demo.repository;

import com.example.demo.model.ArLedger;
import com.example.demo.model.Customer;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArLedgerRepository extends JpaRepository<ArLedger, Long> {
    Page<ArLedger> findByCustomer(Customer customer, Pageable pageable);
}
