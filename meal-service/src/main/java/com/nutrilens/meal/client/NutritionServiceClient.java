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
            return FoodItemDTO.builder()
                    .foodCode(foodCode)
                    .displayName(foodCode)
                    .densityGPerCm3(1.0)
                    .kcalPer100g(150.0)
                    .proteinPer100g(10.0)
                    .carbPer100g(15.0)
                    .fatPer100g(5.0)
                    .build();
        }

        @Override
        public FoodItemDTO getFoodById(Long id) {
            return FoodItemDTO.builder()
                    .id(id)
                    .foodCode("FOOD_" + id)
                    .displayName("Food Item " + id)
                    .densityGPerCm3(1.0)
                    .kcalPer100g(150.0)
                    .proteinPer100g(10.0)
                    .carbPer100g(15.0)
                    .fatPer100g(5.0)
                    .build();
        }

        @Override
        public NutritionResolutionDTO resolveFood(String foodCode, Double volumeCm3, Double portionRatio, Double userWeightKg) {
            double vol = volumeCm3 != null ? volumeCm3 : 150.0;
            double ratio = portionRatio != null ? portionRatio : 1.0;
            double effVol = vol * ratio;
            double weight = effVol * 1.0;
            double kcal = weight * 1.5;
            return NutritionResolutionDTO.builder()
                    .foodCode(foodCode)
                    .foodName(foodCode)
                    .densityGPerCm3(1.0)
                    .rawVolumeCm3(vol)
                    .portionRatio(ratio)
                    .effectiveVolumeCm3(effVol)
                    .estimatedWeightG(weight)
                    .calculatedKcal(kcal)
                    .proteinG(weight * 0.1)
                    .carbG(weight * 0.15)
                    .fatG(weight * 0.05)
                    .confidenceScore(0.90)
                    .build();
        }
    }
}
