package com.example.access_guard.dto.captcha;

public record CaptchaSolveRequest(String captchaId, String answer) {}