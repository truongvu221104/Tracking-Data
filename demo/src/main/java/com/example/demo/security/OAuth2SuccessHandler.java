package com.example.demo.security;

import com.example.demo.authen.JwtUtil;
import com.example.demo.model.AppUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;   // ✅ dùng lại class bạn đã có

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // principal được trả về từ CustomOAuth2UserService
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        AppUser user = principal.getUser();

        // Claims giống lúc bạn login username/password
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("roles", user.getRoles());
        claims.put("emailVerified", user.getEmailVerified());

        // subject: chọn cùng kiểu mà JwtAuthFilter đang dùng (thường là username)
        String jwt = jwtUtil.generate(user.getUsername(), claims);

        // Redirect về FE, gửi token qua query param
        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/callback") // FE callback
                .queryParam("token", jwt).build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
