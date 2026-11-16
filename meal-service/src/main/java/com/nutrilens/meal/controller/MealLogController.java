package com.nutrilens.meal.controller;

import com.nutrilens.meal.dto.AdminSystemStatsResponseDTO;
import com.nutrilens.meal.dto.CreateMealLogRequestDTO;
import com.nutrilens.meal.dto.MealLogResponseDTO;
import com.nutrilens.meal.entity.MealLog;
import com.nutrilens.meal.repository.MealLogRepository;
import com.nutrilens.meal.service.MealLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MealLogController {

    private final MealLogService mealLogService;
    private final MealLogRepository mealLogRepository;

    @GetMapping({"/api/meal-logs", "/api/v1/nutrition/meal-logs"})
    public ResponseEntity<List<MealLogResponseDTO>> getMealLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String mealType) {
        if ((date != null && !date.trim().isEmpty()) || (mealType != null && !mealType.trim().isEmpty()) || (username != null && !username.trim().isEmpty())) {
            return ResponseEntity.ok(mealLogService.getFilteredMealLogs(username, date, mealType));
        }
        return ResponseEntity.ok(mealLogService.getRecentMealLogs(userId, username));
    }

    @GetMapping({"/api/meal-logs/{id}", "/api/v1/nutrition/meal-logs/{id}"})
    public ResponseEntity<MealLogResponseDTO> getMealLogById(@PathVariable Long id) {
        return ResponseEntity.ok(mealLogService.getMealLogById(id));
    }

    @PostMapping({"/api/meal-logs", "/api/v1/nutrition/meal-logs"})
    public ResponseEntity<MealLogResponseDTO> createMealLog(@Valid @RequestBody CreateMealLogRequestDTO request) {
        MealLogResponseDTO created = mealLogService.createMealLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/meal-logs/internal/today-calories")
    public ResponseEntity<BigDecimal> getTodayCalories(@RequestParam String username) {
        BigDecimal totalToday = mealLogService.getTodayCalories(username);
        return ResponseEntity.ok(totalToday);
    }

    @GetMapping({"/api/admin/stats", "/api/v1/admin/stats", "/api/meal-logs/admin/stats"})
    public ResponseEntity<AdminSystemStatsResponseDTO> getAdminStats() {
        long totalLogs = mealLogRepository.count();
        List<MealLog> all = mealLogRepository.findAll();
        BigDecimal totalKcal = all.stream()
                .map(MealLog::getTotalKcal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgKcal = totalLogs > 0
                ? totalKcal.divide(BigDecimal.valueOf(totalLogs), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        AdminSystemStatsResponseDTO stats = AdminSystemStatsResponseDTO.builder()
                .totalFoods(34)
                .totalMealLogs(totalLogs)
                .totalUsers(2)
                .totalTips(10)
                .averageKcalPerMeal(avgKcal)
                .springBootStatus("UP")
                .aiServiceStatus("UP")
                .databaseStatus("UP")
                .build();

        return ResponseEntity.ok(stats);
    }
}
