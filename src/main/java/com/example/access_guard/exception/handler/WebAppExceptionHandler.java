package com.example.access_guard.exception.handler;

import com.example.access_guard.exception.*;
import com.example.access_guard.exception.IllegalArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;



@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebAppExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Error handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return new Error("Неверный email или пароль", extractPath(request));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Error handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex,
            WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return new Error("Ошибка аутентификации", extractPath(request));
    }

    @ExceptionHandler(RefreshTokenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Error handleRefreshTokenException(RefreshTokenException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleAlreadyExists(AlreadyExistsException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Error handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        return buildResponse(ex, request);
    }


    @ExceptionHandler(DifferentPasswordsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleDifferentPasswords(DifferentPasswordsException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Error handleInvalidToken(IllegalArgumentException ex, WebRequest request) {
        return buildResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Error handleAuthenticationCredentialsNotFound(AuthenticationCredentialsNotFoundException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse("Необходима полная аутентификация для доступа к этому ресурсу", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Error handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse("У вас нет прав для доступа к этому ресурсу", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.info("Validation failed: {}", message);
        return buildResponse(message, request);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleBindException(BindException ex, WebRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.info("Bind failed: {}", message);
        return buildResponse(message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String message = ex.getMessage();
        log.info("Constraint violations: {}", message);
        return buildResponse(message, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Error handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String message = "Метод '" + ex.getMethod() + "' не поддерживается для этого запроса";
        log.warn(message);
        return buildResponse(message, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Error handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildResponse(ex.getMessage(), request);
    }

    // Вспомогательные методы
    private Error buildResponse(Exception ex, WebRequest webRequest) {
        log.debug("Building error response for exception: {}", ex.getClass().getSimpleName());
        String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        return new Error(message, extractPath(webRequest));
    }

    private Error buildResponse(String message, WebRequest webRequest) {
        log.debug("Building error response with custom message: {}", message);
        return new Error(message, extractPath(webRequest));
    }

    private String extractPath(WebRequest webRequest) {
        String desc = webRequest.getDescription(false); // Обычно возвращает строку вида "uri=/path"
        if (desc != null && desc.startsWith("uri=")) {
            return desc.substring(4);
        }
        return desc;
    }
}