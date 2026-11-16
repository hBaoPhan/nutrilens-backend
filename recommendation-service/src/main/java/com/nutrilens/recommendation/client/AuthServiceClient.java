package com.nutrilens.recommendation.client;

import com.nutrilens.common.dto.response.UserSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "auth-service", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @GetMapping("/api/auth/internal/users/{username}")
    UserSummaryDTO getUserSummary(@PathVariable("username") String username);

    @GetMapping("/api/auth/internal/users/id/{id}")
    UserSummaryDTO getUserSummaryById(@PathVariable("id") Long id);
}

@Component
class AuthServiceClientFallback implements AuthServiceClient {
    @Override
    public UserSummaryDTO getUserSummary(String username) {
        return UserSummaryDTO.builder()
                .id(1L)
                .username(username != null ? username : "guest")
                .fullName("Guest User")
                .dailyCalorieGoal(2000)
                .fitnessGoal("MAINTAIN")
                .weightKg(65.0)
                .build();
    }

    @Override
    public UserSummaryDTO getUserSummaryById(Long id) {
        return getUserSummary("user_" + id);
    }
}
