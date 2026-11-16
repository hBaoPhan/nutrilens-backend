package com.nutrilens.nutrition.dto;

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
public class MultiNutritionAnalysisResponseDTO {
    private String summaryTitle;
    private Integer totalComponentsCount;
    private BigDecimal totalEffectiveVolumeCm3;
    private BigDecimal totalEstimatedWeightGrams;
    private BigDecimal totalCaloriesKcal;
    private BigDecimal totalProteinGrams;
    private BigDecimal totalCarbGrams;
    private BigDecimal totalFatGrams;
    private Integer runningMinutesNeeded;
    private Integer briskWalkingMinutesNeeded;
    private List<ComponentNutritionResultDTO> components;
}
