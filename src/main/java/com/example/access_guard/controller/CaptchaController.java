package com.example.access_guard.controller;

import com.example.access_guard.dto.captcha.CaptchaCreateResponse;
import com.example.access_guard.dto.captcha.CaptchaSolveRequest;
import com.example.access_guard.dto.captcha.CaptchaSolveResponse;
import com.example.access_guard.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/captcha")
@RequiredArgsConstructor
@CrossOrigin // при необходимости настройте origin
public class CaptchaController {

    private final CaptchaService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaptchaCreateResponse> create() {
        return ResponseEntity.ok(service.createCaptcha());
    }

    @PostMapping(value = "/solve",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaptchaSolveResponse> solve(@RequestBody CaptchaSolveRequest request) {
        return ResponseEntity.ok(service.solveCaptcha(request.captchaId(), request.answer()));
    }
}
