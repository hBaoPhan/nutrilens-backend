package com.nutrilens.nutrition.service;

import com.nutrilens.common.dto.response.FoodItemDTO;
import com.nutrilens.common.dto.response.NutritionResolutionDTO;
import com.nutrilens.nutrition.dto.FoodItemResponseDTO;
import com.nutrilens.nutrition.dto.MultiNutritionAnalysisResponseDTO;
import com.nutrilens.nutrition.dto.MultiVolumetricNutritionRequestDTO;
import com.nutrilens.nutrition.dto.NutritionAnalysisResponseDTO;
import com.nutrilens.nutrition.dto.VolumetricNutritionRequestDTO;

import java.util.List;

public interface NutritionEngineService {
    NutritionAnalysisResponseDTO analyzeVolumetricPortion(VolumetricNutritionRequestDTO request);
    MultiNutritionAnalysisResponseDTO analyzeMultiVolumetricPortion(MultiVolumetricNutritionRequestDTO request);
    List<FoodItemResponseDTO> getActiveFoods(String category);
    List<FoodItemDTO> getActiveFoodDTOs(String category);
    FoodItemDTO getFoodByCode(String foodCode);
    FoodItemDTO getFoodById(Long id);
    NutritionResolutionDTO resolveSingleFood(String foodCode, Double volumeCm3, Double portionRatio, Double userWeightKg);
}
