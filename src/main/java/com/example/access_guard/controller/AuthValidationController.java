package com.example.access_guard.controller;

import com.example.access_guard.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class AuthValidationController {
    private final AuthService authService;

    @GetMapping()
    public String validateToken(@RequestHeader(AUTHORIZATION) String authHeader) {
        return authHeader + "   Токен валиден: не протух и подпись корректна.";
    }
}