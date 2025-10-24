package com.example.demo.repository;

import com.example.demo.model.Customer;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCode(String code);

    Page<Customer> findByNameContainingIgnoreCase(String q, Pageable pageable);
}
