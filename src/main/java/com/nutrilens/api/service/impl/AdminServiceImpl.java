package com.nutrilens.api.service.impl;

import com.nutrilens.api.dto.request.CreateFoodItemRequestDTO;
import com.nutrilens.api.dto.response.AdminSystemStatsResponseDTO;
import com.nutrilens.api.dto.response.FoodItemResponseDTO;
import com.nutrilens.api.entity.FoodDensity;
import com.nutrilens.api.entity.MealLog;
import com.nutrilens.api.exception.EntityNotFoundException;
import com.nutrilens.api.repository.FoodDensityRepository;
import com.nutrilens.api.repository.MealLogRepository;
import com.nutrilens.api.repository.NutritionTipRepository;
import com.nutrilens.api.repository.UserRepository;
import com.nutrilens.api.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final FoodDensityRepository foodDensityRepository;
    private final MealLogRepository mealLogRepository;
    private final UserRepository userRepository;
    private final NutritionTipRepository nutritionTipRepository;

    public AdminServiceImpl(FoodDensityRepository foodDensityRepository,
                            MealLogRepository mealLogRepository,
                            UserRepository userRepository,
                            NutritionTipRepository nutritionTipRepository) {
        this.foodDensityRepository = foodDensityRepository;
        this.mealLogRepository = mealLogRepository;
        this.userRepository = userRepository;
        this.nutritionTipRepository = nutritionTipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSystemStatsResponseDTO getSystemStats() {
        long totalFoods = foodDensityRepository.count();
        long totalMealLogs = mealLogRepository.count();
        long totalUsers = userRepository.count();
        long totalTips = nutritionTipRepository.count();

        // Calculate average kcal per meal
        List<MealLog> recentLogs = mealLogRepository.findAll();
        BigDecimal avgKcal = BigDecimal.ZERO;
        if (!recentLogs.isEmpty()) {
            BigDecimal totalKcalSum = recentLogs.stream()
                    .map(m -> m.getTotalKcal() != null ? m.getTotalKcal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgKcal = totalKcalSum.divide(BigDecimal.valueOf(recentLogs.size()), 1, RoundingMode.HALF_UP);
        }

        return AdminSystemStatsResponseDTO.builder()
                .totalFoods(totalFoods)
                .totalMealLogs(totalMealLogs)
                .totalUsers(totalUsers)
                .totalTips(totalTips)
                .averageKcalPerMeal(avgKcal)
                .springBootStatus("UP")
                .aiServiceStatus("UP")
                .databaseStatus("UP")
                .build();
    }

    @Override
    @Transactional
    public FoodItemResponseDTO createFood(CreateFoodItemRequestDTO request) {
        if (foodDensityRepository.findByFoodCode(request.getFoodCode().trim()).isPresent()) {
            throw new IllegalArgumentException("Mã món ăn đã tồn tại: " + request.getFoodCode());
        }

        FoodDensity food = FoodDensity.builder()
                .foodCode(request.getFoodCode().trim().toUpperCase())
                .displayName(request.getDisplayName().trim())
                .category(request.getCategory() != null ? request.getCategory().trim() : "Lunch")
                .foodType(request.getFoodType() != null ? request.getFoodType().trim() : "COMPOSITE_DISH")
                .densityGramsPerCm3(request.getDensityGramsPerCm3())
                .kcalPer100g(request.getKcalPer100g())
                .proteinPer100g(request.getProteinPer100g())
                .carbPer100g(request.getCarbPer100g())
                .fatPer100g(request.getFatPer100g())
                .servingWeightG(request.getServingWeightG() != null ? request.getServingWeightG() : BigDecimal.valueOf(100))
                .servingVolumeCm3(request.getServingVolumeCm3() != null ? request.getServingVolumeCm3() : BigDecimal.valueOf(150))
                .emoji(request.getEmoji() != null ? request.getEmoji().trim() : "🍲")
                .iconType(request.getIconType() != null ? request.getIconType().trim() : "salad")
                .iconBgColor(request.getIconBgColor() != null ? request.getIconBgColor().trim() : "#FFEFE6")
                .iconColor(request.getIconColor() != null ? request.getIconColor().trim() : "#F37340")
                .isActive(true)
                .build();

        FoodDensity saved = foodDensityRepository.save(food);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public FoodItemResponseDTO updateFood(Long id, CreateFoodItemRequestDTO request) {
        FoodDensity existing = foodDensityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy món ăn với ID: " + id));

        existing.setDisplayName(request.getDisplayName().trim());
        existing.setCategory(request.getCategory() != null ? request.getCategory().trim() : existing.getCategory());
        existing.setDensityGramsPerCm3(request.getDensityGramsPerCm3());
        existing.setKcalPer100g(request.getKcalPer100g());
        existing.setProteinPer100g(request.getProteinPer100g());
        existing.setCarbPer100g(request.getCarbPer100g());
        existing.setFatPer100g(request.getFatPer100g());
        if (request.getEmoji() != null) existing.setEmoji(request.getEmoji().trim());
        if (request.getServingWeightG() != null) existing.setServingWeightG(request.getServingWeightG());
        if (request.getServingVolumeCm3() != null) existing.setServingVolumeCm3(request.getServingVolumeCm3());

        FoodDensity updated = foodDensityRepository.save(existing);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteFood(Long id) {
        FoodDensity existing = foodDensityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy món ăn với ID: " + id));
        existing.setIsActive(false);
        foodDensityRepository.save(existing);
    }

    private FoodItemResponseDTO mapToDTO(FoodDensity food) {
        BigDecimal weight = food.getServingWeightG() != null ? food.getServingWeightG() : BigDecimal.valueOf(100);
        BigDecimal factor = weight.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal totalKcal = food.getKcalPer100g().multiply(factor).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalProtein = food.getProteinPer100g().multiply(factor).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalCarbs = food.getCarbPer100g().multiply(factor).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalFat = food.getFatPer100g().multiply(factor).setScale(0, RoundingMode.HALF_UP);

        return FoodItemResponseDTO.builder()
                .id(food.getId())
                .foodCode(food.getFoodCode())
                .name(food.getDisplayName())
                .category(food.getCategory())
                .foodType(food.getFoodType())
                .kcal(totalKcal)
                .weightG(weight)
                .volumeCm3(food.getServingVolumeCm3())
                .proteinG(totalProtein)
                .carbsG(totalCarbs)
                .fatG(totalFat)
                .emoji(food.getEmoji())
                .iconType(food.getIconType())
                .iconBgColor(food.getIconBgColor())
                .iconColor(food.getIconColor())
                .isHighlightedMore(Boolean.TRUE.equals(food.getIsHighlightedMore()))
                .build();
    }
}
