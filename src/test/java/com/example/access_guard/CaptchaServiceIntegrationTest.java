package com.example.access_guard;

import com.example.access_guard.dto.captcha.CaptchaCreateResponse;
import com.example.access_guard.dto.captcha.CaptchaSolveResponse;
import com.example.access_guard.exception.CaptchaException;
import com.example.access_guard.service.CaptchaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;


import static org.junit.jupiter.api.Assertions.*;

class CaptchaServiceIntegrationTest extends AbstractTest {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    void createCaptcha_shouldStoreChallengeAndReturnImage() {
        CaptchaCreateResponse resp = captchaService.createCaptcha();
        assertNotNull(resp);
        assertNotNull(resp.captchaId());
        assertNotNull(resp.imageBase64());
        assertTrue(resp.imageBase64().startsWith("data:image/png;base64,"));
        assertTrue(resp.expiresAtEpochSec() > 0);

        String key = "captcha:challenge:" + resp.captchaId();
        String storedValue = redis.opsForValue().get(key);
        assertNotNull(storedValue);
    }

    @Test
    void solveCaptcha_withCorrectAnswer_shouldReturnTokenAndDeleteChallenge() {
        CaptchaCreateResponse create = captchaService.createCaptcha();
        String id = create.captchaId();
        String key = "captcha:challenge:" + create.captchaId();
        String storedValue = redis.opsForValue().get(key);
        CaptchaSolveResponse solve = captchaService.solveCaptcha(id, storedValue);
        assertNotNull(solve);
        assertNotNull(solve.captchaToken());
        assertTrue(solve.expiresAtEpochSec() > 0);

        String tokenKey = "captcha:token:" + solve.captchaToken();
        assertEquals("valid", redis.opsForValue().get(tokenKey));

        // challenge должен быть удалён
        assertNull(redis.opsForValue().get("captcha:challenge:" + id));
    }

    @Test
    void solveCaptcha_withWrongAnswer_shouldThrowAndDeleteChallenge() {
        CaptchaCreateResponse create = captchaService.createCaptcha();
        String id = create.captchaId();

        CaptchaException ex = assertThrows(CaptchaException.class, () -> captchaService.solveCaptcha(id, "wrong"));
        assertNotNull(ex.getMessage());
        assertNull(redis.opsForValue().get("captcha:challenge:" + id));
    }

    @Test
    void solveCaptcha_withMissingId_shouldThrow() {
        assertThrows(CaptchaException.class, () -> captchaService.solveCaptcha("non-existent-id-123", "whatever"));
    }

    @Test
    void isCaptchaTokenInvalid_behavior() {
        captchaService.storeCaptchaToken();
        assertFalse(captchaService.isCaptchaTokenInvalid("token"));
        assertTrue(captchaService.isCaptchaTokenInvalid("token"));
        assertFalse(captchaService.isCaptchaTokenInvalid(null));
    }
}
