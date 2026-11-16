package com.nutrilens.meal.dto;

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
public class CreateMealItemRequestDTO {

    @NotBlank(message = "Food code is required")
    private String foodCode;

    private String foodName;

    @Builder.Default
    private BigDecimal portionRatio = BigDecimal.ONE;

    private BigDecimal rawVolumeCm3;

    private BigDecimal effectiveVolumeCm3;

    private BigDecimal estimatedWeightG;

    @NotNull(message = "Calculated kcal is required")
    private BigDecimal calculatedKcal;

    private BigDecimal proteinG;

    private BigDecimal carbG;

    private BigDecimal fatG;

    @Builder.Default
    private BigDecimal confidenceScore = new BigDecimal("0.950");
}
