package com.nutrilens.nutrition.controller;

import com.nutrilens.common.dto.response.ApiResponse;
import com.nutrilens.common.dto.response.FoodItemDTO;
import com.nutrilens.common.dto.response.NutritionResolutionDTO;
import com.nutrilens.nutrition.dto.*;
import com.nutrilens.nutrition.service.NutritionEngineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
@Slf4j
public class NutritionEngineController {

    private final NutritionEngineService nutritionEngineService;

    @PostMapping("/resolve-volume")
    public ResponseEntity<ApiResponse<NutritionAnalysisResponseDTO>> resolveVolume(
            @Valid @RequestBody VolumetricNutritionRequestDTO request) {
        log.info("[NUTRITION SERVICE] Resolving volume for food code: {}, volume: {} cm3", request.getFoodCode(), request.getVolumeCm3());
        NutritionAnalysisResponseDTO response = nutritionEngineService.analyzeVolumetricPortion(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Volumetric portion analysis resolved"));
    }

    @PostMapping("/resolve-volume-multi")
    public ResponseEntity<ApiResponse<MultiNutritionAnalysisResponseDTO>> resolveVolumeMulti(
            @Valid @RequestBody MultiVolumetricNutritionRequestDTO request) {
        log.info("[NUTRITION SERVICE] Resolving multi-component volume (count: {})", request.getComponents().size());
        MultiNutritionAnalysisResponseDTO response = nutritionEngineService.analyzeMultiVolumetricPortion(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Multi-component volumetric analysis resolved"));
    }

    @GetMapping("/foods")
    public ResponseEntity<ApiResponse<List<FoodItemResponseDTO>>> getFoods(
            @RequestParam(required = false, defaultValue = "ALL") String category) {
        List<FoodItemResponseDTO> foods = nutritionEngineService.getActiveFoods(category);
        return ResponseEntity.ok(ApiResponse.success(foods, "Retrieved " + foods.size() + " active foods"));
    }

    // =========================================================================
    // Internal Microservice Endpoints (Invoked via OpenFeign)
    // =========================================================================
    @GetMapping("/internal/foods/{foodCode}")
    public ResponseEntity<FoodItemDTO> getInternalFoodByCode(@PathVariable String foodCode) {
        FoodItemDTO dto = nutritionEngineService.getFoodByCode(foodCode);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/internal/foods/id/{id}")
    public ResponseEntity<FoodItemDTO> getInternalFoodById(@PathVariable Long id) {
        FoodItemDTO dto = nutritionEngineService.getFoodById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/internal/resolve")
    public ResponseEntity<NutritionResolutionDTO> getInternalResolvedFood(
            @RequestParam String foodCode,
            @RequestParam(required = false, defaultValue = "150.0") Double volumeCm3,
            @RequestParam(required = false, defaultValue = "1.0") Double portionRatio,
            @RequestParam(required = false, defaultValue = "65.0") Double userWeightKg) {
        NutritionResolutionDTO dto = nutritionEngineService.resolveSingleFood(foodCode, volumeCm3, portionRatio, userWeightKg);
        return ResponseEntity.ok(dto);
    }
}
