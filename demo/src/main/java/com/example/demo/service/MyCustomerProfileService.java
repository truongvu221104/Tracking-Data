package com.example.demo.service;

import com.example.demo.dto.CustomerProfileRequest;
import com.example.demo.dto.CustomerProfileResponse;
import com.example.demo.model.AppUser;
import com.example.demo.model.Customer;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MyCustomerProfileService {

    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;

    // --- Helper: lấy AppUser hiện tại từ SecurityContext ---

    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }

        String username = auth.getName(); // giả định username lưu trong token
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("AppUser not found: " + username));
    }

    // --- Helper: lấy hoặc tạo mới Customer cho user hiện tại ---

    private Customer getOrCreateCustomerForCurrentUser(AppUser user) {
        return customerRepository.findByUser(user).orElseGet(() -> {
            Instant now = Instant.now();
            String code = "CUS" + user.getId();

            Customer c = Customer.builder()
                    .code(code)
                    .user(user)
                    .name(user.getUsername())  // tạm, sau user sẽ cập nhật lại
                    .phone(null)
                    .note(null)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            return customerRepository.save(c);
        });
    }


    @Transactional
    public CustomerProfileResponse getMyProfile() {
        AppUser user = getCurrentUser();
        Customer customer = getOrCreateCustomerForCurrentUser(user);
        return CustomerProfileResponse.fromEntity(customer);
    }

    @Transactional
    public CustomerProfileResponse upsertMyProfile(CustomerProfileRequest req) {
        AppUser user = getCurrentUser();
        Customer customer = getOrCreateCustomerForCurrentUser(user);

        customer.setName(req.getName());
        customer.setPhone(req.getPhone());
        customer.setNote(req.getNote());
        customer.setUpdatedAt(Instant.now());

        Customer saved = customerRepository.save(customer);
        return CustomerProfileResponse.fromEntity(saved);
    }
}
