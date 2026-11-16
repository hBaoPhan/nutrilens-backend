package com.nutrilens.recommendation.client;

import com.nutrilens.common.dto.response.FoodItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@FeignClient(name = "nutrition-service", fallback = NutritionServiceClientFallback.class)
public interface NutritionServiceClient {

    @GetMapping("/api/nutrition/foods")
    List<FoodItemDTO> getFoods(@RequestParam(value = "category", required = false) String category);
}

@Component
class NutritionServiceClientFallback implements NutritionServiceClient {
    @Override
    public List<FoodItemDTO> getFoods(String category) {
        return Collections.singletonList(
                FoodItemDTO.builder()
                        .id(1L)
                        .foodCode("VIET_COM_TAM")
                        .displayName("Cơm Tấm Sườn Bì Chả")
                        .category("Lunch")
                        .densityGPerCm3(0.85)
                        .kcalPer100g(159.0)
                        .proteinPer100g(7.8)
                        .carbPer100g(22.5)
                        .fatPer100g(4.2)
                        .build()
        );
    }
}
