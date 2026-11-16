package com.nutrilens.nutrition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFoodRequestDTO {

    @NotBlank(message = "Food code is required")
    private String foodCode;

    @NotBlank(message = "Display name is required")
    private String displayName;

    private String category;
    private String foodType;

    @NotNull(message = "Density is required")
    private BigDecimal densityGramsPerCm3;

    @NotNull(message = "Kcal per 100g is required")
    private BigDecimal kcalPer100g;

    private BigDecimal proteinPer100g;
    private BigDecimal carbPer100g;
    private BigDecimal fatPer100g;
    private BigDecimal servingWeightG;
    private BigDecimal servingVolumeCm3;
    private String emoji;
    private String iconType;
    private String iconBgColor;
    private String iconColor;
    private Boolean isHighlightedMore;
}
