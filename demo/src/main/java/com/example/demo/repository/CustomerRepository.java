package com.example.demo.repository;

import com.example.demo.model.AppUser;
import com.example.demo.model.Customer;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCode(String code);
    Optional<Customer> findByUser(AppUser user);
    Page<Customer> findByNameContainingIgnoreCase(String q, Pageable pageable);
}
