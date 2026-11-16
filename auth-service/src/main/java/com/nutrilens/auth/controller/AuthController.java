package com.nutrilens.auth.controller;

import com.nutrilens.auth.dto.AuthResponseDTO;
import com.nutrilens.auth.dto.LoginRequestDTO;
import com.nutrilens.auth.dto.RegisterRequestDTO;
import com.nutrilens.auth.dto.UpdateProfileRequestDTO;
import com.nutrilens.auth.service.AuthService;
import com.nutrilens.common.dto.response.ApiResponse;
import com.nutrilens.common.dto.response.UserSummaryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "User registered successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> getProfile(
            @RequestHeader(value = "X-User-Username", required = false) String gatewayUsername,
            Authentication authentication) {
        String username = gatewayUsername != null && !gatewayUsername.isBlank()
                ? gatewayUsername
                : (authentication != null ? authentication.getName() : null);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthenticated user"));
        }

        AuthResponseDTO profile = authService.getCurrentUser(username);
        return ResponseEntity.ok(ApiResponse.success(profile, "User profile retrieved"));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> updateProfile(
            @RequestHeader(value = "X-User-Username", required = false) String gatewayUsername,
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        String username = gatewayUsername != null && !gatewayUsername.isBlank()
                ? gatewayUsername
                : (authentication != null ? authentication.getName() : null);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthenticated user"));
        }

        AuthResponseDTO updated = authService.updateProfile(username, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    // =========================================================================
    // Internal Microservice Endpoints (Invoked via OpenFeign)
    // =========================================================================
    @GetMapping("/internal/users/{username}")
    public ResponseEntity<UserSummaryDTO> getInternalUserSummary(@PathVariable String username) {
        UserSummaryDTO summary = authService.getUserSummary(username);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/internal/users/id/{id}")
    public ResponseEntity<UserSummaryDTO> getInternalUserSummaryById(@PathVariable Long id) {
        UserSummaryDTO summary = authService.getUserSummaryById(id);
        return ResponseEntity.ok(summary);
    }
}
