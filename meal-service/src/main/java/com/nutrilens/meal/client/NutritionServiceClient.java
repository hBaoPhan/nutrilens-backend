package com.nutrilens.meal.client;

import com.nutrilens.common.dto.response.FoodItemDTO;
import com.nutrilens.common.dto.response.NutritionResolutionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "nutrition-service", fallback = NutritionServiceClient.NutritionServiceClientFallback.class)
public interface NutritionServiceClient {

    @GetMapping("/api/nutrition/internal/foods/{foodCode}")
    FoodItemDTO getFoodByCode(@PathVariable("foodCode") String foodCode);

    @GetMapping("/api/nutrition/internal/foods/id/{id}")
    FoodItemDTO getFoodById(@PathVariable("id") Long id);

    @GetMapping("/api/nutrition/internal/resolve")
    NutritionResolutionDTO resolveFood(
            @RequestParam("foodCode") String foodCode,
            @RequestParam("volumeCm3") Double volumeCm3,
            @RequestParam("portionRatio") Double portionRatio,
            @RequestParam("userWeightKg") Double userWeightKg);

    @Component
    class NutritionServiceClientFallback implements NutritionServiceClient {
        @Override
        public FoodItemDTO getFoodByCode(String foodCode) {
            return null;
        }

        @Override
        public FoodItemDTO getFoodById(Long id) {
            return null;
        }

        @Override
        public NutritionResolutionDTO resolveFood(String foodCode, Double volumeCm3, Double portionRatio, Double userWeightKg) {
            return null;
        }
    }
}
