// src/main/java/com/example/demo/service/CustomerAdminService.java
package com.example.demo.service;

import com.example.demo.dto.CustomerAdminDto;
import com.example.demo.dto.CustomerAdminUpdateRequest;
import com.example.demo.model.Customer;
import com.example.demo.model.AppUser;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAdminService {

    private final CustomerRepository customerRepository;

    public List<CustomerAdminDto> getAll() {
        return customerRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public CustomerAdminDto getById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return toDto(c);
    }

    @Transactional
    public CustomerAdminDto update(Long id, CustomerAdminUpdateRequest req) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (req.getName() != null) {
            c.setName(req.getName().trim());
        }
        if (req.getPhone() != null) {
            c.setPhone(req.getPhone().trim());
        }
        if (req.getNote() != null) {
            c.setNote(req.getNote());
        }
        if (req.getEnabled() != null && c.getUser() != null) {
            c.getUser().setEnabled(req.getEnabled());
        }
        return toDto(c);
    }

    @Transactional
    public void delete(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (c.getUser() != null) {
            c.getUser().setEnabled(false);
        }
    }

    private CustomerAdminDto toDto(Customer c) {
        AppUser u = c.getUser();

        return CustomerAdminDto.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .phone(c.getPhone())
                .note(c.getNote())
                .userId(u != null ? u.getId() : null)
                .email(u != null ? u.getEmail() : null)
                .enabled(u != null ? u.getEnabled() : null)
                .createdAt(c.getCreatedAt())
                .build();
    }
}
