package com.nutrilens.recommendation.dto;

import com.nutrilens.common.dto.response.FoodItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealRecommendationResponseDTO {
    private String username;
    private String currentMealType;
    private String recommendedDish;
    private String foodCode;
    private BigDecimal targetDailyKcal;
    private BigDecimal consumedTodayKcal;
    private BigDecimal remainingTodayKcal;
    private String status; // "ON_TRACK", "DEFICIT", "SURPLUS"
    private String healthGoal;
    private String aiAdvice;
    private List<FoodItemDTO> candidateDishes;
}
