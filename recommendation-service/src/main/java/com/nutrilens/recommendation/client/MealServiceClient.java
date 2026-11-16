package com.nutrilens.recommendation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "meal-service", fallback = MealServiceClientFallback.class)
public interface MealServiceClient {

    @GetMapping("/api/meal-logs/internal/today-calories")
    BigDecimal getTodayCalories(@RequestParam("username") String username);
}

@Component
class MealServiceClientFallback implements MealServiceClient {
    @Override
    public BigDecimal getTodayCalories(String username) {
        return BigDecimal.ZERO;
    }
}
