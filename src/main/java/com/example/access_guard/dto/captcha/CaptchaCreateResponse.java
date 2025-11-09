package com.example.access_guard.dto.captcha;

public record CaptchaCreateResponse(String captchaId, String imageBase64, long expiresAtEpochSec) {}