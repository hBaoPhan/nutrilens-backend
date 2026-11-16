package com.nutrilens.recommendation.service;

import com.nutrilens.recommendation.dto.MealRecommendationResponseDTO;

public interface MealRecommendationService {
    MealRecommendationResponseDTO getNextMealRecommendation(String username);
}
