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
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        response.setSuccess(true);
        response.setMessage("Login successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        response.setSuccess(true);
        response.setMessage("User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"/profile", "/me"})
    public ResponseEntity<AuthResponseDTO> getProfile(
            @RequestParam(required = false) String username,
            @RequestHeader(value = "X-User-Username", required = false) String gatewayUsername,
            Authentication authentication) {
        String targetUser = (username != null && !username.isBlank())
                ? username.trim()
                : (gatewayUsername != null && !gatewayUsername.isBlank()
                    ? gatewayUsername.trim()
                    : (authentication != null ? authentication.getName() : null));

        if (targetUser == null || targetUser.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResponseDTO profile = authService.getCurrentUser(targetUser);
        profile.setSuccess(true);
        profile.setMessage("User profile retrieved");
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<AuthResponseDTO> updateProfile(
            @RequestParam(required = false) String username,
            @RequestHeader(value = "X-User-Username", required = false) String gatewayUsername,
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        String targetUser = (username != null && !username.isBlank())
                ? username.trim()
                : (gatewayUsername != null && !gatewayUsername.isBlank()
                    ? gatewayUsername.trim()
                    : (authentication != null ? authentication.getName() : null));

        if (targetUser == null || targetUser.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResponseDTO updated = authService.updateProfile(targetUser, request);
        updated.setSuccess(true);
        updated.setMessage("Profile updated successfully");
        return ResponseEntity.ok(updated);
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
