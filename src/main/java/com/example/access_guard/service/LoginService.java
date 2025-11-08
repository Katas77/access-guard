package com.example.access_guard.service;

import com.example.access_guard.dto.request.LoginRequest;
import com.example.access_guard.dto.response.AuthResponse;
import com.example.access_guard.security.SecurityService;
import com.example.access_guard.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final SecurityService securityService;
    private final JwtUtils jwtUtils;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        return securityService.authenticateUser(loginRequest);
    }

    public AuthResponse refreshToken(String refreshToken) {
        return securityService.refreshToken(refreshToken);
    }

    public boolean validateToken(String headerAuth) {
        if (!StringUtils.hasText(headerAuth) || !headerAuth.startsWith("Bearer ")) {
            return false;
        }
        try {
            String token = headerAuth.substring(7);
            return jwtUtils.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}