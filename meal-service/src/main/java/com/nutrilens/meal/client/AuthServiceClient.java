package com.nutrilens.meal.client;

import com.nutrilens.common.dto.response.UserSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", fallback = AuthServiceClient.AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @GetMapping("/api/auth/internal/users/{username}")
    UserSummaryDTO getUserSummary(@PathVariable("username") String username);

    @GetMapping("/api/auth/internal/users/id/{id}")
    UserSummaryDTO getUserSummaryById(@PathVariable("id") Long id);

    @Component
    class AuthServiceClientFallback implements AuthServiceClient {
        @Override
        public UserSummaryDTO getUserSummary(String username) {
            return UserSummaryDTO.builder()
                    .id(0L)
                    .username(username)
                    .fullName(username)
                    .weightKg(65.0)
                    .heightCm(170.0)
                    .dailyCalorieGoal(2000)
                    .role("ROLE_USER")
                    .build();
        }

        @Override
        public UserSummaryDTO getUserSummaryById(Long id) {
            return UserSummaryDTO.builder()
                    .id(id)
                    .username("user_" + id)
                    .fullName("NutriLens User")
                    .weightKg(65.0)
                    .heightCm(170.0)
                    .dailyCalorieGoal(2000)
                    .role("ROLE_USER")
                    .build();
        }
    }
}
