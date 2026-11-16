package com.nutrilens.meal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMealLogRequestDTO {

    private Long userId;

    private String username;

    @NotBlank(message = "Meal type is required (Breakfast, Lunch, Dinner)")
    private String mealType;

    private LocalDateTime loggedAt;

    private BigDecimal totalVolumeCm3;

    private BigDecimal totalWeightG;

    @NotNull(message = "Total kcal is required")
    private BigDecimal totalKcal;

    private BigDecimal totalProteinG;

    private BigDecimal totalCarbG;

    private BigDecimal totalFatG;

    private String arSnapshotUrl;

    private String userNotes;

    @NotEmpty(message = "At least one meal item must be included")
    @Valid
    private List<CreateMealItemRequestDTO> items;
}
