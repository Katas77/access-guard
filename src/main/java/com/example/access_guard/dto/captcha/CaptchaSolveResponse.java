package com.example.access_guard.dto.captcha;

public record CaptchaSolveResponse(String captchaToken, long expiresAtEpochSec) {}