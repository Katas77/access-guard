package com.example.access_guard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth2")
@RequiredArgsConstructor
public class AppController {


    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String validateToken() {
        return "loginService.validateToken(headerAuth)";
    }
}
