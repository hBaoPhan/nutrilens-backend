package com.nutrilens.meal.service;

import com.nutrilens.meal.dto.CreateMealLogRequestDTO;
import com.nutrilens.meal.dto.MealLogResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface MealLogService {
    List<MealLogResponseDTO> getRecentMealLogs(Long userId, String username);
    List<MealLogResponseDTO> getFilteredMealLogs(String username, String date, String mealType);
    MealLogResponseDTO getMealLogById(Long id);
    MealLogResponseDTO createMealLog(CreateMealLogRequestDTO request);
    BigDecimal getTodayCalories(String username);
}
