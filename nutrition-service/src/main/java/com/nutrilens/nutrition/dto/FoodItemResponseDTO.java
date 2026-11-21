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
public class FoodItemResponseDTO {
    private Long id;
    private String foodCode;
    private String name;
    private String displayName;
    private String category;
    private String foodType;
    private BigDecimal kcal;
    private BigDecimal weightG;
    private BigDecimal volumeCm3;
    private BigDecimal densityGPerCm3;
    private BigDecimal densityGramsPerCm3;
    private BigDecimal kcalPer100g;
    private BigDecimal proteinPer100g;
    private BigDecimal carbPer100g;
    private BigDecimal fatPer100g;
    private BigDecimal proteinG;
    private BigDecimal carbsG;
    private BigDecimal fatG;
    private String emoji;
    private String iconType;
    private String iconBgColor;
    private String iconColor;
    private Boolean isHighlightedMore;
}
