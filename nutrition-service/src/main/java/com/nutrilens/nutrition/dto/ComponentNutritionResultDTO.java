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
public class ComponentNutritionResultDTO {
    private String foodCode;
    private String displayName;
    private String foodType;
    private String emoji;
    private BigDecimal effectiveVolumeCm3;
    private BigDecimal estimatedWeightGrams;
    private BigDecimal caloriesKcal;
    private BigDecimal proteinGrams;
    private BigDecimal carbGrams;
    private BigDecimal fatGrams;
}
