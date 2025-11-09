package com.example.access_guard.service;

import com.example.access_guard.dto.captcha.CaptchaCreateResponse;
import com.example.access_guard.dto.captcha.CaptchaSolveResponse;
import com.example.access_guard.exception.CaptchaException;
import com.google.code.kaptcha.Producer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {
    private static final long CAPTCHA_TTL_SECONDS = 120;
    private static final long TOKEN_TTL_SECONDS   = 120; // срок одноразового токена

    private final Producer kaptchaProducer;
    private final StringRedisTemplate redis;

    public CaptchaService(Producer kaptchaProducer, StringRedisTemplate redis) {
        this.kaptchaProducer = kaptchaProducer;
        this.redis = redis;
    }

    public CaptchaCreateResponse createCaptcha() {
        String text = kaptchaProducer.createText();
        BufferedImage img = kaptchaProducer.createImage(text);

        String id = UUID.randomUUID().toString();
        redis.opsForValue().set("captcha:challenge:" + id, text.toLowerCase(), CAPTCHA_TTL_SECONDS, TimeUnit.SECONDS);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            long expiresAt = Instant.now().getEpochSecond() + CAPTCHA_TTL_SECONDS;
            return new CaptchaCreateResponse(id, "data:image/png;base64," + base64, expiresAt);
        } catch (IOException e) {
            throw new RuntimeException("Failed to render captcha image", e);
        }
    }


    public CaptchaSolveResponse solveCaptcha(String captchaId, String answer) {
        if (captchaId == null || answer == null) {
            throw new IllegalArgumentException("captchaId or answer is null");
        }

        String key = "captcha:challenge:" + captchaId;
        String expected = redis.opsForValue().get(key);

        if (expected == null) {
            throw new CaptchaException("Captcha expired or not found");
        }

        // Удаляем challenge сразу — чтобы предотвратить повторное использование
        redis.delete(key);

        if (!expected.equalsIgnoreCase(answer.trim())) {
            throw new CaptchaException("Captcha answer incorrect");
        }

        // Генерируем одноразовый токен, записываем его в Redis с TTL
        String token = UUID.randomUUID().toString();
        String tokenKey = "captcha:token:" + token;
        redis.opsForValue().set(tokenKey, "valid", TOKEN_TTL_SECONDS, TimeUnit.SECONDS);

        long expiresAt = Instant.now().getEpochSecond() + TOKEN_TTL_SECONDS;
        return new CaptchaSolveResponse(token, expiresAt);
    }


    public boolean verifyTokenAndConsume(String token) {
        this.storeCaptchaToken();
        if (token == null) return false;
        String tokenKey = "captcha:token:" + token;
        Boolean exists = redis.hasKey(tokenKey);
        if (exists) {
            redis.delete(tokenKey);
            return false;
        }
        return true;
    }

    private void storeCaptchaToken() {
        String tokenKey = "captcha:token:" + "token";
        redis.opsForValue().set(tokenKey, "valid", TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
    }

}
