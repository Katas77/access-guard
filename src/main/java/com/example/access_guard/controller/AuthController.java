package com.example.access_guard.controller;

import com.example.access_guard.dto.request.CreateUserRequest;
import com.example.access_guard.dto.request.LoginRequest;
import com.example.access_guard.dto.request.RefreshRequest;
import com.example.access_guard.dto.response.AuthResponse;
import com.example.access_guard.dto.response.BasicResponse;
import com.example.access_guard.security.SecurityService;
import com.example.access_guard.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final SecurityService securityService;
    @PostMapping("/register")
    public ResponseEntity<BasicResponse> registerUser(@RequestBody CreateUserRequest request) {
        securityService.register(request);
        return ResponseEntity
                .status(201) // CREATED
                .body(new BasicResponse(LocalDateTime.now(), "Пользователь успешно создан"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = loginService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        AuthResponse response = loginService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        boolean isValid = loginService.validateToken(authHeader);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        securityService.logout();
        return ResponseEntity.ok().build();
    }
}