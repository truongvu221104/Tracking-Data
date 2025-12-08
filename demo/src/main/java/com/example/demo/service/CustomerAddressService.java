package com.example.demo.service;

import com.example.demo.dto.AddressRequest;
import com.example.demo.dto.AddressResponse;
import com.example.demo.model.AppUser;
import com.example.demo.model.Customer;
import com.example.demo.model.CustomerAddress;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.CustomerAddressRepository;
import com.example.demo.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {

    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;

    // --- Helper: lấy user hiện tại từ SecurityContext ---
    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("Người dùng chưa được xác thực");
        }

        String username = auth.getName(); // nếu em dùng username trong token
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy AppUser: " + username));
    }

    // --- Helper: lấy Customer hiện tại của user ---
    private Customer getCurrentCustomer() {
        AppUser user = getCurrentUser();
        return customerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ khách hàng cho người dùng hiện tại"));
    }

    // --- API logic ---

    @Transactional
    public List<AddressResponse> getMyAddresses() {
        Customer customer = getCurrentCustomer();
        return addressRepository.findByCustomerOrderByIsDefaultDescCreatedAtDesc(customer)
                .stream()
                .map(AddressResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AddressResponse createMyAddress(AddressRequest request) {
        Customer customer = getCurrentCustomer();

        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());

        if (isDefault) {
            // Bỏ mặc định các địa chỉ cũ
            addressRepository.clearDefaultForCustomer(customer);
        } else {
            // Nếu khách chưa có địa chỉ nào -> địa chỉ đầu tiên auto là default
            boolean hasAnyAddress = !addressRepository
                    .findByCustomerOrderByIsDefaultDescCreatedAtDesc(customer)
                    .isEmpty();
            if (!hasAnyAddress) {
                isDefault = true;
            }
        }

        Instant now = Instant.now();

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .label(request.getLabel())
                .countryCode(request.getCountryCode())
                .provinceCode(request.getProvinceCode())
                .districtCode(request.getDistrictCode())
                .addressLine(request.getAddressLine())
                .isDefault(isDefault)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .createdAt(now)
                .build();

        CustomerAddress saved = addressRepository.save(address);
        return AddressResponse.fromEntity(saved);
    }

    @Transactional
    public AddressResponse updateMyAddress(Long addressId, AddressRequest request) {
        Customer customer = getCurrentCustomer();

        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));

        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalStateException("Bạn không được phép sửa địa chỉ này");
        }

        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());

        if (isDefault) {
            addressRepository.clearDefaultForCustomer(customer);
        }

        address.setLabel(request.getLabel());
        address.setCountryCode(request.getCountryCode());
        address.setProvinceCode(request.getProvinceCode());
        address.setDistrictCode(request.getDistrictCode());
        address.setAddressLine(request.getAddressLine());
        address.setIsDefault(isDefault);
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setUpdatedAt(Instant.now());

        CustomerAddress saved = addressRepository.save(address);
        return AddressResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteMyAddress(Long addressId) {
        Customer customer = getCurrentCustomer();

        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));

        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalStateException("Bạn không được phép xoá địa chỉ này");
        }

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        addressRepository.delete(address);

        if (wasDefault) {
            List<CustomerAddress> remaining = addressRepository
                    .findByCustomerOrderByIsDefaultDescCreatedAtDesc(customer);
            if (!remaining.isEmpty()) {
                CustomerAddress first = remaining.get(0);
                first.setIsDefault(true);
                first.setUpdatedAt(Instant.now());
                addressRepository.save(first);
            }
        }
    }
}
