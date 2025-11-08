package com.example.access_guard.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AuthResponse(Long id,
                           LocalDateTime timestamp,
                           String accessToken,
                           String refreshToken,
                           String email,
                           String name,
                           List<String> roles) {}