package com.example.access_guard.controller;

import com.example.access_guard.dto.request.CreateUserRequest;
import com.example.access_guard.dto.request.LoginRequest;
import com.example.access_guard.dto.request.RefreshRequest;
import com.example.access_guard.dto.response.AuthResponse;
import com.example.access_guard.dto.response.BasicResponse;
import com.example.access_guard.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;


    @PostMapping("/register")
    public ResponseEntity<BasicResponse> registerUser(@RequestBody CreateUserRequest request) {
        service.register(request);
        return ResponseEntity
                .status(201)
                .body(new BasicResponse(LocalDateTime.now(), "Пользователь успешно создан"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = service.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        AuthResponse response = service.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        boolean isValid = service.validateToken(authHeader);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        service.logout();
        return ResponseEntity.ok().build();
    }
}