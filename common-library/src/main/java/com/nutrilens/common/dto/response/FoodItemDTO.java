package com.nutrilens.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemDTO {
    private Long id;
    private String foodCode;
    private String displayName;
    private String category;
    private Double densityGPerCm3;
    private Double kcalPer100g;
    private Double proteinPer100g;
    private Double carbPer100g;
    private Double fatPer100g;
    private Double servingWeightG;
    private Double servingVolumeCm3;
    private String emoji;
    private String iconType;
    private String iconBgColor;
    private String iconColor;
    private Boolean isHighlightedMore;
    private Double fiberPer100g;
    private Boolean isActive;
}
