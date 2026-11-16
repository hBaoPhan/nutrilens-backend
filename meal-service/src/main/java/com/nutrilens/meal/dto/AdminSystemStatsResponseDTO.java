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
public class AdminSystemStatsResponseDTO {
    private long totalFoods;
    private long totalMealLogs;
    private long totalUsers;
    private long totalTips;
    private BigDecimal averageKcalPerMeal;
    private String springBootStatus;
    private String aiServiceStatus;
    private String databaseStatus;
}
