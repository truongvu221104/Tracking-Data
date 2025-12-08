package com.example.demo.controller;

import com.example.demo.dto.AddressRequest;
import com.example.demo.dto.AddressResponse;
import com.example.demo.service.CustomerAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/addresses")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    @GetMapping
    public List<AddressResponse> getMyAddresses() {
        return addressService.getMyAddresses();
    }

    @PostMapping
    public AddressResponse createMyAddress(@RequestBody AddressRequest request) {
        return addressService.createMyAddress(request);
    }

    @PutMapping("/{id}")
    public AddressResponse updateMyAddress(
            @PathVariable Long id,
            @RequestBody AddressRequest request
    ) {
        return addressService.updateMyAddress(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteMyAddress(@PathVariable Long id) {
        addressService.deleteMyAddress(id);
    }
}
