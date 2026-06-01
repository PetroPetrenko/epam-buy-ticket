package com.example.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth API", description = "Авторизация и регистрация")
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Демо-эндпоинт для авторизации")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Демо-реализация
        return ResponseEntity.ok(AuthResponse.builder()
                .token("demo-jwt-token")
                .username(request.getUsername())
                .build());
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @Builder
    public static class AuthResponse {
        private String token;
        private String username;
    }
}
