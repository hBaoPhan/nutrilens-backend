package com.nutrilens.api.service;

import com.nutrilens.api.dto.response.MealRecommendationResponseDTO;

public interface MealRecommendationService {
    MealRecommendationResponseDTO getNextMealRecommendation(Long userId, String username);
}
