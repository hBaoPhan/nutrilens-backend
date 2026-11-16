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
            return null;
        }

        @Override
        public UserSummaryDTO getUserSummaryById(Long id) {
            return null;
        }
    }
}
