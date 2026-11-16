package com.nutrilens.nutrition.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumetricNutritionRequestDTO {

    @NotBlank(message = "Food identifier code is required")
    private String foodCode;

    @NotNull(message = "Scanned volume in cm3 is required")
    @DecimalMin(value = "0.50", message = "Volume must be at least 0.50 cm3")
    @DecimalMax(value = "4000.00", message = "Volume exceeds plate physical limit")
    private BigDecimal volumeCm3;

    @NotNull(message = "Portion ratio is required")
    @DecimalMin(value = "0.05", message = "Portion ratio cannot be under 5%")
    @DecimalMax(value = "1.00", message = "Portion ratio cannot exceed 100%")
    @Builder.Default
    private BigDecimal portionRatio = BigDecimal.ONE;

    @NotNull(message = "User body weight is required for MET calculation")
    @DecimalMin(value = "30.0", message = "User weight must be at least 30.0 kg")
    @DecimalMax(value = "250.0", message = "User weight exceeds maximum allowable limit")
    private BigDecimal userWeightKg;
}
