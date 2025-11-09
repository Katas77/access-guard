package com.example.access_guard.service;


import com.example.access_guard.dto.request.CreateUserRequest;
import com.example.access_guard.dto.request.LoginRequest;
import com.example.access_guard.dto.response.AuthResponse;
import com.example.access_guard.exception.AlreadyExistsException;
import com.example.access_guard.exception.RefreshTokenException;
import com.example.access_guard.model.RoleTypeAuth;
import com.example.access_guard.model.postgres.User;
import com.example.access_guard.repository.UserRepository;
import com.example.access_guard.security.AppUserDetails;
import com.example.access_guard.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthResponse authenticateUser(LoginRequest request) {
        String email = request.email().trim();
        String password = request.password();

        if (email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Email and password must not be blank");
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, password);

        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        AppUserDetails userDetails = (AppUserDetails) auth.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        String accessToken = jwtUtils.generateTokenFromEmail(userDetails.getEmail());
        var refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new AuthResponse(
                userDetails.getId(),
                LocalDateTime.now(),
                accessToken,
                refreshToken.getToken(),
                userDetails.getEmail(),
                userDetails.getName(),
                roles
        );
    }

    // === Регистрация ===
    public void register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password1()))
                .roles(Collections.singleton(RoleTypeAuth.ROLE_USER))
                .build();

        userRepository.save(user);
    }

    // === Обновление токенов ===
    public AuthResponse refreshToken(String refreshTokenValue) {
        return refreshTokenService.findByRefreshToken(refreshTokenValue)
                .map(refreshTokenService::checkRefreshToken)
                .map(refreshToken -> {
                    User user = userRepository.findById(refreshToken.getUserId())
                            .orElseThrow(() -> new RefreshTokenException("User not found"));
                    String newAccessToken = jwtUtils.generateTokenFromEmail(user.getEmail());
                    var newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
                    return new AuthResponse(
                            user.getId(),
                            LocalDateTime.now(),
                            newAccessToken,
                            newRefreshToken.getToken(),
                            user.getEmail(),
                            user.getName(),
                            user.getRoles().stream()
                                    .map(RoleTypeAuth::name)
                                    .toList()
                    );
                })
                .orElseThrow(() -> new RefreshTokenException("Invalid refresh token"));
    }

    // === Выход из системы ===
    public void logout() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserDetails userDetails) {
            refreshTokenService.deleteByUserId(userDetails.getId());
        }
    }

    // === Валидация токена из заголовка (для /validate-token) ===
    public boolean validateToken(String headerAuth) {
        if (!StringUtils.hasText(headerAuth) || !headerAuth.startsWith("Bearer ")) {
            return false;
        }
        try {
            String token = headerAuth.substring(7);
            return jwtUtils.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}