package com.example.access_guard;

import com.example.access_guard.dto.request.CreateUserRequest;
import com.example.access_guard.dto.response.AuthResponse;
import com.example.access_guard.exception.AlreadyExistsException;
import com.example.access_guard.exception.RefreshTokenException;
import com.example.access_guard.model.RoleTypeAuth;
import com.example.access_guard.model.postgres.User;
import com.example.access_guard.repository.UserRepository;
import com.example.access_guard.service.AuthService;
import com.example.access_guard.service.CaptchaService;
import com.example.access_guard.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;


class AuthServiceIntegrationTest extends AbstractTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    protected CaptchaService captchaService;

    @Test
    void register_ValidRequest_CreatesUser() {

        captchaService.storeCaptchaToken();

        String email = "newuser@example.com";
        String password = "securePassword123";
        String name = "New User";
        String captchaToken = "token";

        CreateUserRequest request = new CreateUserRequest(email, password, name, captchaToken);
        authService.register(request);
        User savedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AssertionError("User was not saved"));

        assertEquals(email, savedUser.getEmail());
        assertEquals(name, savedUser.getName());
        assertNotNull(savedUser.getPassword());
        assertNotEquals(password, savedUser.getPassword()); // пароль зашифрован
        assertEquals(1, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().contains(RoleTypeAuth.ROLE_USER));
    }

    @Test
    void register_DuplicateEmail_ThrowsAlreadyExistsException() {
        captchaService.storeCaptchaToken();
        String existingEmail = "existing@example.com"; // из insert_users.sql
        String password = "anotherPassword";
        String name = "Another User";
        String captchaToken = "token";

        CreateUserRequest request = new CreateUserRequest(existingEmail, password, name, captchaToken);

        AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> authService.register(request),
                "Expected AlreadyExistsException for duplicate email"
        );
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void refreshToken_ValidToken_ReturnsNewTokens() {
        Long userId = 3L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AssertionError("Test user not found in DB"));

        var initialRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        AuthResponse response = authService.refreshToken(initialRefreshToken.getToken());

        assertNotNull(response);
        assertEquals(userId, response.id());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getName(), response.name());
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertNotEquals(initialRefreshToken.getToken(), response.refreshToken()); // должен быть новый токен
    }

    @Test
    void refreshToken_InvalidToken_ThrowsRefreshTokenException() {
        String invalidToken = "nonExistentToken";

        RefreshTokenException exception = assertThrows(
                RefreshTokenException.class,
                () -> authService.refreshToken(invalidToken),
                "Expected RefreshTokenException for invalid token"
        );
        assertTrue(exception.getMessage().contains("Invalid refresh token") || exception.getMessage().contains("User not found"));
    }
}