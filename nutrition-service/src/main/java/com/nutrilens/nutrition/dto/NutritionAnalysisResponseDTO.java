package com.nutrilens.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionAnalysisResponseDTO {
    private String foodName;
    private BigDecimal effectiveVolumeCm3;
    private BigDecimal estimatedWeightGrams;
    private BigDecimal totalCaloriesKcal;
    private BigDecimal proteinGrams;
    private BigDecimal carbGrams;
    private BigDecimal fatGrams;
    private Integer runningMinutesNeeded;
    private Integer briskWalkingMinutesNeeded;
}
