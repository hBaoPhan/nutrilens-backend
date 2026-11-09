package com.nutrilens.api.service.impl;

import com.nutrilens.api.dto.request.LoginRequestDTO;
import com.nutrilens.api.dto.request.RegisterRequestDTO;
import com.nutrilens.api.dto.request.UpdateProfileRequestDTO;
import com.nutrilens.api.dto.response.AuthResponseDTO;
import com.nutrilens.api.entity.User;
import com.nutrilens.api.exception.EntityNotFoundException;
import com.nutrilens.api.repository.UserRepository;
import com.nutrilens.api.security.JwtTokenProvider;
import com.nutrilens.api.service.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap.admin.username:}")
    private String bootstrapAdminUsername;

    @Value("${app.bootstrap.admin.password:}")
    private String bootstrapAdminPassword;

    @Value("${app.bootstrap.admin.email:admin@nutrilens.com}")
    private String bootstrapAdminEmail;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostConstruct
    public void bootstrapAdminIfConfigured() {
        if (!bootstrapAdminEnabled) {
            log.info("[AUTH SERVICE] Default account auto-creation is disabled (app.bootstrap.admin.enabled=false). Users must register via /api/v1/auth/register.");
            return;
        }

        if (bootstrapAdminUsername == null || bootstrapAdminUsername.trim().isEmpty() ||
            bootstrapAdminPassword == null || bootstrapAdminPassword.trim().isEmpty()) {
            log.warn("[AUTH SERVICE] Admin bootstrap is enabled but APP_BOOTSTRAP_ADMIN_USERNAME or APP_BOOTSTRAP_ADMIN_PASSWORD is empty. Skipping admin creation.");
            return;
        }

        String username = bootstrapAdminUsername.trim();
        User admin = userRepository.findByUsername(username).orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .username(username)
                    .email(bootstrapAdminEmail != null && !bootstrapAdminEmail.trim().isEmpty() ? bootstrapAdminEmail.trim() : username + "@nutrilens.com")
                    .passwordHash(passwordEncoder.encode(bootstrapAdminPassword))
                    .fullName("NutriLens System Administrator")
                    .weightKg(new BigDecimal("70.00"))
                    .heightCm(new BigDecimal("175.00"))
                    .role("ROLE_ADMIN")
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("[AUTH SERVICE] Securely bootstrapped admin user '{}' from environment configuration.", username);
        } else {
            log.info("[AUTH SERVICE] Admin user '{}' already exists in database. Preserving existing record.", username);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("[AUTH SERVICE] Validating login for user '{}'", request.getUsername());
        User user = userRepository.findByUsernameAndIsActiveTrue(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("[AUTH SERVICE] Login rejected: user '{}' not found or inactive", request.getUsername());
                    return new EntityNotFoundException("User not found or account inactive: " + request.getUsername());
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("[AUTH SERVICE] Login rejected: invalid password for user '{}'", request.getUsername());
            throw new IllegalArgumentException("Invalid password credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername());
        log.info("[AUTH SERVICE] Token generated successfully for user '{}' with role '{}'", user.getUsername(), user.getRole());

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

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("[AUTH SERVICE] Processing registration for user '{}', email '{}'", request.getUsername(), request.getEmail());
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("[AUTH SERVICE] Register rejected: username '{}' already exists", request.getUsername());
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[AUTH SERVICE] Register rejected: email '{}' already registered", request.getEmail());
            throw new IllegalArgumentException("Email is already registered");
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
        String token = jwtTokenProvider.generateToken(savedUser.getUsername());

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .weightKg(savedUser.getWeightKg())
                .heightCm(savedUser.getHeightCm())
                .dailyCalorieGoal(savedUser.getDailyCalorieGoal())
                .gender(savedUser.getGender())
                .age(savedUser.getAge())
                .activityLevel(savedUser.getActivityLevel())
                .fitnessGoal(savedUser.getFitnessGoal())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO getCurrentUser(String username) {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        return AuthResponseDTO.builder()
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
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDTO updateProfile(String username, UpdateProfileRequestDTO request) {
        log.info("[AUTH SERVICE] Updating personalized profile for user '{}': weight={}, height={}, dailyCalorieGoal={}",
                username, request.getWeightKg(), request.getHeightCm(), request.getDailyCalorieGoal());

        User user = userRepository.findByUsernameAndIsActiveTrue(username)
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
        log.info("[AUTH SERVICE] User '{}' profile successfully updated.", username);

        return AuthResponseDTO.builder()
                .userId(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .fullName(updatedUser.getFullName())
                .role(updatedUser.getRole())
                .weightKg(updatedUser.getWeightKg())
                .heightCm(updatedUser.getHeightCm())
                .dailyCalorieGoal(updatedUser.getDailyCalorieGoal())
                .gender(updatedUser.getGender())
                .age(updatedUser.getAge())
                .activityLevel(updatedUser.getActivityLevel())
                .fitnessGoal(updatedUser.getFitnessGoal())
                .tokenType("Bearer")
                .build();
    }
}
