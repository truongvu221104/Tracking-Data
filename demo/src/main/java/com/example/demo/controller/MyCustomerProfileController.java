package com.example.demo.controller;

import com.example.demo.dto.CustomerProfileRequest;
import com.example.demo.dto.CustomerProfileResponse;
import com.example.demo.service.MyCustomerProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/customer")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "My Customer Profile")
public class MyCustomerProfileController {

    private final MyCustomerProfileService service;

    @GetMapping
    public CustomerProfileResponse getMyProfile() {
        return service.getMyProfile();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public CustomerProfileResponse upsertMyProfile(
            @RequestBody @Validated CustomerProfileRequest request
    ) {
        return service.upsertMyProfile(request);
    }
}
