package com.nutrilens.auth.service.impl;

import com.nutrilens.auth.dto.AuthResponseDTO;
import com.nutrilens.auth.dto.LoginRequestDTO;
import com.nutrilens.auth.dto.RegisterRequestDTO;
import com.nutrilens.auth.dto.UpdateProfileRequestDTO;
import com.nutrilens.auth.entity.User;
import com.nutrilens.auth.repository.UserRepository;
import com.nutrilens.auth.security.JwtTokenProvider;
import com.nutrilens.auth.service.AuthService;
import com.nutrilens.common.dto.response.UserSummaryDTO;
import com.nutrilens.common.exception.BadRequestException;
import com.nutrilens.common.exception.EntityNotFoundException;
import com.nutrilens.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("[AUTH SERVICE] Login attempt for user: {}", request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .filter(User::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("User not found or account inactive: " + request.getUsername()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("[AUTH SERVICE] Invalid password for user: {}", request.getUsername());
            throw new UnauthorizedException("Invalid password credentials");
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("[AUTH SERVICE] Login successful for user: {}", user.getUsername());
        return mapToAuthResponse(user, token);
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("[AUTH SERVICE] Registering new user: {}, email: {}", request.getUsername(), request.getEmail());
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .weightKg(request.getWeightKg() != null ? request.getWeightKg() : new BigDecimal("65.00"))
                .heightCm(new BigDecimal("170.00"))
                .dailyCalorieGoal(2000)
                .gender("MALE")
                .age(25)
                .activityLevel("SEDENTARY")
                .fitnessGoal("MAINTAIN")
                .role("ROLE_USER")
                .isActive(true)
                .build();

        User savedUser = userRepository.save(newUser);
        String token = jwtTokenProvider.generateToken(savedUser);
        return mapToAuthResponse(savedUser, token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .filter(User::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return mapToAuthResponse(user, null);
    }

    @Override
    @Transactional
    public AuthResponseDTO updateProfile(String username, UpdateProfileRequestDTO request) {
        log.info("[AUTH SERVICE] Updating profile for user '{}'", username);
        User user = userRepository.findByUsername(username)
                .filter(User::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        user.setWeightKg(request.getWeightKg());
        user.setHeightCm(request.getHeightCm());
        user.setDailyCalorieGoal(request.getDailyCalorieGoal());

        if (request.getGender() != null && !request.getGender().isBlank()) {
            user.setGender(request.getGender().toUpperCase());
        }
        if (request.getAge() != null && request.getAge() > 0) {
            user.setAge(request.getAge());
        }
        if (request.getActivityLevel() != null && !request.getActivityLevel().isBlank()) {
            user.setActivityLevel(request.getActivityLevel());
        }
        if (request.getFitnessGoal() != null && !request.getFitnessGoal().isBlank()) {
            user.setFitnessGoal(request.getFitnessGoal());
        }

        User updatedUser = userRepository.save(user);
        return mapToAuthResponse(updatedUser, null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDTO getUserSummary(String username) {
        User user = userRepository.findByUsername(username)
                .filter(User::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return mapToSummary(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDTO getUserSummaryById(Long id) {
        User user = userRepository.findById(id)
                .filter(User::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("User ID not found: " + id));
        return mapToSummary(user);
    }

    private AuthResponseDTO mapToAuthResponse(User user, String token) {
        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .weightKg(user.getWeightKg())
                .heightCm(user.getHeightCm())
                .dailyCalorieGoal(user.getDailyCalorieGoal())
                .gender(user.getGender())
                .age(user.getAge())
                .activityLevel(user.getActivityLevel())
                .fitnessGoal(user.getFitnessGoal())
                .build();
    }

    private UserSummaryDTO mapToSummary(User user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .weightKg(user.getWeightKg() != null ? user.getWeightKg().doubleValue() : 65.0)
                .heightCm(user.getHeightCm() != null ? user.getHeightCm().doubleValue() : 170.0)
                .gender(user.getGender())
                .dailyCalorieGoal(user.getDailyCalorieGoal())
                .age(user.getAge())
                .activityLevel(user.getActivityLevel())
                .fitnessGoal(user.getFitnessGoal())
                .role(user.getRole())
                .build();
    }
}
