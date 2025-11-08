package com.example.access_guard.dto.response;

import java.time.LocalDateTime;

public record BasicResponse(LocalDateTime timestamp, String data) {}