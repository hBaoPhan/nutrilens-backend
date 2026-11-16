package com.nutrilens.meal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealLogResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userFullName;
    private String mealType;
    private String foodName;
    private LocalDateTime loggedAt;
    private String loggedAtDisplay;
    private BigDecimal totalVolumeCm3;
    private BigDecimal totalWeightG;
    private BigDecimal totalKcal;
    private BigDecimal totalProteinG;
    private BigDecimal totalCarbG;
    private BigDecimal totalFatG;
    private String arSnapshotUrl;
    private String userNotes;
    private int runningMinutesNeeded;
    private int walkingMinutesNeeded;
    @Builder.Default
    private List<MealItemResponseDTO> items = new ArrayList<>();
}
