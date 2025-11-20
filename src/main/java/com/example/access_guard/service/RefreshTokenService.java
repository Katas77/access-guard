package com.example.access_guard.service;

import com.example.access_guard.exception.RefreshTokenException;
import com.example.access_guard.model.redis.RefreshToken;
import com.example.access_guard.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    private final RefreshTokenRepository repository;

    public Optional<RefreshToken> findByRefreshToken(String token) {
        return repository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        String tokenValue = UUID.randomUUID().toString();
        long id = System.nanoTime();
        var refreshToken = RefreshToken.builder()
                .id(id)
                .userId(userId)
                .token(tokenValue)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration.toMillis()))
                .build();
        return repository.save(refreshToken);
    }

    public RefreshToken checkRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token was expired. Please sign in again.");
        }
        return refreshToken;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        List<RefreshToken> tokens = repository.findByUserId(userId);
        if (!tokens.isEmpty()) {
            repository.deleteAll(tokens);
        }
    }
}