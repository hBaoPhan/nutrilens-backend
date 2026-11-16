package com.nutrilens.meal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealItemResponseDTO {
    private Long id;
    private Long foodDensityId;
    private String foodCode;
    private String foodName;
    private String emoji;
    private BigDecimal rawVolumeCm3;
    private BigDecimal portionRatio;
    private BigDecimal effectiveVolumeCm3;
    private BigDecimal estimatedWeightG;
    private BigDecimal calculatedKcal;
    private BigDecimal proteinG;
    private BigDecimal carbG;
    private BigDecimal fatG;
    private BigDecimal confidenceScore;
}
