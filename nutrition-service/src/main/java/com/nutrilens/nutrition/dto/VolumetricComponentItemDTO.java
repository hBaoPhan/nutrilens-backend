package com.nutrilens.nutrition.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class VolumetricComponentItemDTO {

    @NotBlank(message = "Component food identifier code is required")
    private String foodCode;

    @NotNull(message = "Component volume in cm3 is required")
    @DecimalMin(value = "0.50", message = "Component volume must be at least 0.50 cm3")
    @DecimalMax(value = "4000.00", message = "Component volume exceeds allowable physical limit")
    private BigDecimal volumeCm3;

    @NotNull(message = "Component portion ratio is required")
    @DecimalMin(value = "0.05", message = "Portion ratio cannot be under 5%")
    @DecimalMax(value = "1.00", message = "Portion ratio cannot exceed 100%")
    @Builder.Default
    private BigDecimal portionRatio = BigDecimal.ONE;
}
