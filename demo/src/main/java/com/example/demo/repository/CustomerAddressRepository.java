package com.example.demo.repository;

import com.example.demo.model.Customer;
import com.example.demo.model.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByCustomerOrderByIsDefaultDescCreatedAtDesc(Customer customer);

    @Modifying
    @Query("update CustomerAddress a set a.isDefault = false " +
            "where a.customer = :customer and a.isDefault = true")
    void clearDefaultForCustomer(@Param("customer") Customer customer);
}
