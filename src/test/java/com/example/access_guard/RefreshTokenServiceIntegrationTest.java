package com.example.access_guard;

import com.example.access_guard.exception.RefreshTokenException;
import com.example.access_guard.model.redis.RefreshToken;
import com.example.access_guard.repository.RefreshTokenRepository;
import com.example.access_guard.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


public class RefreshTokenServiceIntegrationTest extends AbstractTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void createRefreshToken_savesAndCanBeFound() {
        long userId = 123L;
        RefreshToken token = refreshTokenService.createRefreshToken(userId);

        assertThat(token).isNotNull();
        assertThat(token.getId()).isNotNull();
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getExpiryDate()).isAfter(Instant.now());
        Optional<RefreshToken> found = refreshTokenService.findByRefreshToken(token.getToken());
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void checkRefreshToken_expired_throwsAndDeletesFromRepo() {
        RefreshToken expired = RefreshToken.builder()
                .id(System.nanoTime())
                .userId(555L)
                .token("expired-token-" + System.nanoTime())
                .expiryDate(Instant.now().minusSeconds(10))
                .build();

        refreshTokenRepository.save(expired);
        assertThat(refreshTokenRepository.findByToken(expired.getToken())).isPresent();
        assertThatThrownBy(() -> refreshTokenService.checkRefreshToken(expired))
                .isInstanceOf(RefreshTokenException.class);
        assertThat(refreshTokenRepository.findByToken(expired.getToken())).isNotPresent();
    }

    @Test
    void deleteByUserId_removesAllTokensForUser() {
        long userId = 999L;

        RefreshToken t1 = RefreshToken.builder()
                .id(System.nanoTime())
                .userId(userId)
                .token("t1-" + System.nanoTime())
                .expiryDate(Instant.now().plusSeconds(60))
                .build();

        RefreshToken t2 = RefreshToken.builder()
                .id(System.nanoTime() + 1)
                .userId(userId)
                .token("t2-" + System.nanoTime())
                .expiryDate(Instant.now().plusSeconds(60))
                .build();
        refreshTokenRepository.saveAll(List.of(t1, t2));
        assertThat(refreshTokenRepository.findByToken(t1.getToken())).isPresent();
        assertThat(refreshTokenRepository.findByToken(t2.getToken())).isPresent();
        refreshTokenService.deleteByUserId(userId);
        assertThat(refreshTokenRepository.findByToken(t1.getToken())).isNotPresent();
        assertThat(refreshTokenRepository.findByToken(t2.getToken())).isNotPresent();
    }
}
