package com.example.demo.security;

import com.example.demo.model.AppUser;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId(); // "google" hoặc "facebook"

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String name;
        String providerUserId;

        if ("google".equalsIgnoreCase(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            providerUserId = (String) attributes.get("sub");
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            providerUserId = (String) attributes.get("id");
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        AppUser user = userService.findOrCreateFromSocial(
                registrationId,
                providerUserId,
                email,
                name
        );

        // Trả về 1 OAuth2User có chứa thông tin userId để successHandler sinh JWT
        return new CustomUserPrincipal(user, attributes);
    }
}
