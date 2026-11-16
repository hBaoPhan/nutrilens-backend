package com.nutrilens.nutrition.controller;

import com.nutrilens.common.exception.EntityNotFoundException;
import com.nutrilens.nutrition.dto.AdminFoodRequestDTO;
import com.nutrilens.nutrition.dto.FoodItemResponseDTO;
import com.nutrilens.nutrition.entity.FoodDensity;
import com.nutrilens.nutrition.repository.FoodDensityRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping({"/api/admin/foods", "/api/v1/admin/foods"})
@RequiredArgsConstructor
@Slf4j
public class AdminFoodController {

    private final FoodDensityRepository foodDensityRepository;

    @PostMapping
    public ResponseEntity<FoodItemResponseDTO> createFood(@Valid @RequestBody AdminFoodRequestDTO request) {
        log.info("[ADMIN FOOD CONTROLLER] Creating food: {}", request.getFoodCode());

        FoodDensity food = FoodDensity.builder()
                .foodCode(request.getFoodCode().trim().toUpperCase())
                .displayName(request.getDisplayName().trim())
                .category(request.getCategory() != null ? request.getCategory() : "Lunch")
                .foodType(request.getFoodType() != null ? request.getFoodType() : "COMPOSITE_DISH")
                .densityGramsPerCm3(request.getDensityGramsPerCm3())
                .kcalPer100g(request.getKcalPer100g())
                .proteinPer100g(request.getProteinPer100g() != null ? request.getProteinPer100g() : BigDecimal.ZERO)
                .carbPer100g(request.getCarbPer100g() != null ? request.getCarbPer100g() : BigDecimal.ZERO)
                .fatPer100g(request.getFatPer100g() != null ? request.getFatPer100g() : BigDecimal.ZERO)
                .servingWeightG(request.getServingWeightG() != null ? request.getServingWeightG() : BigDecimal.valueOf(100))
                .servingVolumeCm3(request.getServingVolumeCm3() != null ? request.getServingVolumeCm3() : BigDecimal.valueOf(150))
                .emoji(request.getEmoji() != null ? request.getEmoji() : "🍲")
                .iconType(request.getIconType() != null ? request.getIconType() : "salad")
                .iconBgColor(request.getIconBgColor() != null ? request.getIconBgColor() : "#FFEFE6")
                .iconColor(request.getIconColor() != null ? request.getIconColor() : "#F37340")
                .isHighlightedMore(Boolean.TRUE.equals(request.getIsHighlightedMore()))
                .isActive(true)
                .build();

        FoodDensity saved = foodDensityRepository.save(food);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodItemResponseDTO> updateFood(
            @PathVariable Long id,
            @Valid @RequestBody AdminFoodRequestDTO request) {
        log.info("[ADMIN FOOD CONTROLLER] Updating food ID: {}", id);

        FoodDensity food = foodDensityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Food density not found for ID: " + id));

        food.setFoodCode(request.getFoodCode().trim().toUpperCase());
        food.setDisplayName(request.getDisplayName().trim());
        if (request.getCategory() != null) food.setCategory(request.getCategory());
        if (request.getFoodType() != null) food.setFoodType(request.getFoodType());
        food.setDensityGramsPerCm3(request.getDensityGramsPerCm3());
        food.setKcalPer100g(request.getKcalPer100g());
        if (request.getProteinPer100g() != null) food.setProteinPer100g(request.getProteinPer100g());
        if (request.getCarbPer100g() != null) food.setCarbPer100g(request.getCarbPer100g());
        if (request.getFatPer100g() != null) food.setFatPer100g(request.getFatPer100g());
        if (request.getServingWeightG() != null) food.setServingWeightG(request.getServingWeightG());
        if (request.getServingVolumeCm3() != null) food.setServingVolumeCm3(request.getServingVolumeCm3());
        if (request.getEmoji() != null) food.setEmoji(request.getEmoji());
        if (request.getIconType() != null) food.setIconType(request.getIconType());
        if (request.getIconBgColor() != null) food.setIconBgColor(request.getIconBgColor());
        if (request.getIconColor() != null) food.setIconColor(request.getIconColor());
        if (request.getIsHighlightedMore() != null) food.setIsHighlightedMore(request.getIsHighlightedMore());

        FoodDensity updated = foodDensityRepository.save(food);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        log.info("[ADMIN FOOD CONTROLLER] Soft deleting food ID: {}", id);

        FoodDensity food = foodDensityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Food density not found for ID: " + id));

        food.setIsActive(false);
        foodDensityRepository.save(food);
        return ResponseEntity.noContent().build();
    }

    private FoodItemResponseDTO mapToResponse(FoodDensity food) {
        BigDecimal weight = food.getServingWeightG() != null ? food.getServingWeightG() : BigDecimal.valueOf(100);
        BigDecimal factor = weight.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return FoodItemResponseDTO.builder()
                .id(food.getId())
                .foodCode(food.getFoodCode())
                .name(food.getDisplayName())
                .category(food.getCategory())
                .foodType(food.getFoodType())
                .kcal(food.getKcalPer100g().multiply(factor).setScale(0, RoundingMode.HALF_UP))
                .weightG(weight)
                .volumeCm3(food.getServingVolumeCm3() != null ? food.getServingVolumeCm3() : BigDecimal.valueOf(150))
                .proteinG(food.getProteinPer100g().multiply(factor).setScale(0, RoundingMode.HALF_UP))
                .carbsG(food.getCarbPer100g().multiply(factor).setScale(0, RoundingMode.HALF_UP))
                .fatG(food.getFatPer100g().multiply(factor).setScale(0, RoundingMode.HALF_UP))
                .emoji(food.getEmoji())
                .iconType(food.getIconType())
                .iconBgColor(food.getIconBgColor())
                .iconColor(food.getIconColor())
                .isHighlightedMore(food.getIsHighlightedMore())
                .build();
    }
}
