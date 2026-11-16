package com.nutrilens.nutrition.service.impl;

import com.nutrilens.common.dto.response.FoodItemDTO;
import com.nutrilens.common.dto.response.MetBurnDTO;
import com.nutrilens.common.dto.response.NutritionResolutionDTO;
import com.nutrilens.common.exception.EntityNotFoundException;
import com.nutrilens.nutrition.dto.*;
import com.nutrilens.nutrition.entity.FoodDensity;
import com.nutrilens.nutrition.repository.FoodDensityRepository;
import com.nutrilens.nutrition.service.NutritionEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionEngineServiceImpl implements NutritionEngineService {

    private final FoodDensityRepository foodDensityRepository;

    private static final BigDecimal RUNNING_MET = BigDecimal.valueOf(8.3);
    private static final BigDecimal WALKING_MET = BigDecimal.valueOf(3.8);
    private static final BigDecimal CYCLING_MET = BigDecimal.valueOf(6.0);
    private static final BigDecimal OXYGEN_FACTOR = BigDecimal.valueOf(3.5);
    private static final BigDecimal MET_DIVISOR = BigDecimal.valueOf(200);

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponseDTO> getActiveFoods(String category) {
        List<FoodDensity> foods = (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL"))
                ? foodDensityRepository.findAllByCategoryIgnoreCaseAndIsActiveTrue(category.trim())
                : foodDensityRepository.findAllByIsActiveTrue();

        return foods.stream().map(this::mapToFoodItemResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemDTO> getActiveFoodDTOs(String category) {
        List<FoodDensity> foods = (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL"))
                ? foodDensityRepository.findAllByCategoryIgnoreCaseAndIsActiveTrue(category.trim())
                : foodDensityRepository.findAllByIsActiveTrue();

        return foods.stream().map(this::mapToFoodItemDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionAnalysisResponseDTO analyzeVolumetricPortion(VolumetricNutritionRequestDTO request) {
        FoodDensity food = foodDensityRepository.findByFoodCodeAndIsActiveTrue(request.getFoodCode())
                .orElseThrow(() -> new EntityNotFoundException("Food density not found: " + request.getFoodCode()));

        BigDecimal effectiveVolume = request.getVolumeCm3()
                .multiply(request.getPortionRatio() != null ? request.getPortionRatio() : BigDecimal.ONE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal estimatedWeight = effectiveVolume
                .multiply(food.getDensityGramsPerCm3())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal factor = estimatedWeight.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal totalKcal = food.getKcalPer100g().multiply(factor).setScale(1, RoundingMode.HALF_UP);
        BigDecimal protein = food.getProteinPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal carb = food.getCarbPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fat = food.getFatPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);

        int runningMins = computeExerciseDuration(totalKcal, RUNNING_MET, request.getUserWeightKg());
        int walkingMins = computeExerciseDuration(totalKcal, WALKING_MET, request.getUserWeightKg());

        return NutritionAnalysisResponseDTO.builder()
                .foodName(food.getDisplayName())
                .effectiveVolumeCm3(effectiveVolume)
                .estimatedWeightGrams(estimatedWeight)
                .totalCaloriesKcal(totalKcal)
                .proteinGrams(protein)
                .carbGrams(carb)
                .fatGrams(fat)
                .runningMinutesNeeded(runningMins)
                .briskWalkingMinutesNeeded(walkingMins)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MultiNutritionAnalysisResponseDTO analyzeMultiVolumetricPortion(MultiVolumetricNutritionRequestDTO request) {
        BigDecimal totalVolume = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalKcal = BigDecimal.ZERO;
        BigDecimal totalP = BigDecimal.ZERO;
        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal totalF = BigDecimal.ZERO;

        List<ComponentNutritionResultDTO> components = new ArrayList<>();

        for (VolumetricComponentItemDTO comp : request.getComponents()) {
            FoodDensity food = foodDensityRepository.findByFoodCodeAndIsActiveTrue(comp.getFoodCode())
                    .orElseThrow(() -> new EntityNotFoundException("Food component not found: " + comp.getFoodCode()));

            BigDecimal effVol = comp.getVolumeCm3()
                    .multiply(comp.getPortionRatio() != null ? comp.getPortionRatio() : BigDecimal.ONE)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal weight = effVol.multiply(food.getDensityGramsPerCm3()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal factor = weight.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            BigDecimal kcal = food.getKcalPer100g().multiply(factor).setScale(1, RoundingMode.HALF_UP);
            BigDecimal p = food.getProteinPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal c = food.getCarbPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal f = food.getFatPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);

            totalVolume = totalVolume.add(effVol);
            totalWeight = totalWeight.add(weight);
            totalKcal = totalKcal.add(kcal);
            totalP = totalP.add(p);
            totalC = totalC.add(c);
            totalF = totalF.add(f);

            components.add(ComponentNutritionResultDTO.builder()
                    .foodCode(food.getFoodCode())
                    .displayName(food.getDisplayName())
                    .foodType(food.getFoodType())
                    .emoji(food.getEmoji())
                    .effectiveVolumeCm3(effVol)
                    .estimatedWeightGrams(weight)
                    .caloriesKcal(kcal)
                    .proteinGrams(p)
                    .carbGrams(c)
                    .fatGrams(f)
                    .build());
        }

        int runningMins = computeExerciseDuration(totalKcal, RUNNING_MET, request.getUserWeightKg());
        int walkingMins = computeExerciseDuration(totalKcal, WALKING_MET, request.getUserWeightKg());

        return MultiNutritionAnalysisResponseDTO.builder()
                .summaryTitle("Phân tích tổng hợp khẩu phần AR")
                .totalComponentsCount(components.size())
                .totalEffectiveVolumeCm3(totalVolume)
                .totalEstimatedWeightGrams(totalWeight)
                .totalCaloriesKcal(totalKcal)
                .totalProteinGrams(totalP)
                .totalCarbGrams(totalC)
                .totalFatGrams(totalF)
                .runningMinutesNeeded(runningMins)
                .briskWalkingMinutesNeeded(walkingMins)
                .components(components)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FoodItemDTO getFoodByCode(String foodCode) {
        FoodDensity food = foodDensityRepository.findByFoodCodeAndIsActiveTrue(foodCode)
                .orElseThrow(() -> new EntityNotFoundException("Food code not found: " + foodCode));
        return mapToFoodItemDTO(food);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodItemDTO getFoodById(Long id) {
        FoodDensity food = foodDensityRepository.findById(id)
                .filter(FoodDensity::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("Food ID not found: " + id));
        return mapToFoodItemDTO(food);
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionResolutionDTO resolveSingleFood(String foodCode, Double volumeCm3, Double portionRatio, Double userWeightKg) {
        FoodDensity food = foodDensityRepository.findByFoodCodeAndIsActiveTrue(foodCode)
                .orElseThrow(() -> new EntityNotFoundException("Food code not found: " + foodCode));

        double vol = volumeCm3 != null ? volumeCm3 : 150.0;
        double ratio = portionRatio != null ? portionRatio : 1.0;
        double effVol = vol * ratio;
        double density = food.getDensityGramsPerCm3() != null ? food.getDensityGramsPerCm3().doubleValue() : 1.0;
        double weight = effVol * density;
        double factor = weight / 100.0;

        double kcal = (food.getKcalPer100g() != null ? food.getKcalPer100g().doubleValue() : 100.0) * factor;
        double p = (food.getProteinPer100g() != null ? food.getProteinPer100g().doubleValue() : 5.0) * factor;
        double c = (food.getCarbPer100g() != null ? food.getCarbPer100g().doubleValue() : 15.0) * factor;
        double f = (food.getFatPer100g() != null ? food.getFatPer100g().doubleValue() : 2.0) * factor;

        double userW = userWeightKg != null ? userWeightKg : 65.0;
        double runBurnPerMin = (8.3 * 3.5 * userW) / 200.0;
        double walkBurnPerMin = (3.8 * 3.5 * userW) / 200.0;
        double cycleBurnPerMin = (6.0 * 3.5 * userW) / 200.0;

        MetBurnDTO metBurn = MetBurnDTO.builder()
                .runningMinutes(runBurnPerMin > 0 ? kcal / runBurnPerMin : 0.0)
                .walkingMinutes(walkBurnPerMin > 0 ? kcal / walkBurnPerMin : 0.0)
                .cyclingMinutes(cycleBurnPerMin > 0 ? kcal / cycleBurnPerMin : 0.0)
                .caloriesBurnedPerMinute(runBurnPerMin)
                .build();

        return NutritionResolutionDTO.builder()
                .foodCode(food.getFoodCode())
                .foodName(food.getDisplayName())
                .densityGPerCm3(density)
                .rawVolumeCm3(vol)
                .portionRatio(ratio)
                .effectiveVolumeCm3(effVol)
                .estimatedWeightG(weight)
                .calculatedKcal(kcal)
                .proteinG(p)
                .carbG(c)
                .fatG(f)
                .confidenceScore(0.95)
                .metBurn(metBurn)
                .build();
    }

    private int computeExerciseDuration(BigDecimal calories, BigDecimal met, BigDecimal userWeightKg) {
        if (calories == null || calories.compareTo(BigDecimal.ZERO) <= 0 || userWeightKg == null || userWeightKg.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal calPerMin = met.multiply(OXYGEN_FACTOR).multiply(userWeightKg).divide(MET_DIVISOR, 4, RoundingMode.HALF_UP);
        if (calPerMin.compareTo(BigDecimal.ZERO) <= 0) return 0;
        return calories.divide(calPerMin, 0, RoundingMode.HALF_UP).intValue();
    }

    private FoodItemResponseDTO mapToFoodItemResponse(FoodDensity food) {
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
                .emoji(food.getEmoji() != null ? food.getEmoji() : "🥗")
                .iconType(food.getIconType() != null ? food.getIconType() : "salad")
                .iconBgColor(food.getIconBgColor() != null ? food.getIconBgColor() : "#FFEFE6")
                .iconColor(food.getIconColor() != null ? food.getIconColor() : "#F37340")
                .isHighlightedMore(Boolean.TRUE.equals(food.getIsHighlightedMore()))
                .build();
    }

    private FoodItemDTO mapToFoodItemDTO(FoodDensity food) {
        return FoodItemDTO.builder()
                .id(food.getId())
                .foodCode(food.getFoodCode())
                .displayName(food.getDisplayName())
                .category(food.getCategory())
                .densityGPerCm3(food.getDensityGramsPerCm3() != null ? food.getDensityGramsPerCm3().doubleValue() : 1.0)
                .kcalPer100g(food.getKcalPer100g() != null ? food.getKcalPer100g().doubleValue() : 100.0)
                .proteinPer100g(food.getProteinPer100g() != null ? food.getProteinPer100g().doubleValue() : 5.0)
                .carbPer100g(food.getCarbPer100g() != null ? food.getCarbPer100g().doubleValue() : 15.0)
                .fatPer100g(food.getFatPer100g() != null ? food.getFatPer100g().doubleValue() : 2.0)
                .servingWeightG(food.getServingWeightG() != null ? food.getServingWeightG().doubleValue() : 100.0)
                .servingVolumeCm3(food.getServingVolumeCm3() != null ? food.getServingVolumeCm3().doubleValue() : 150.0)
                .emoji(food.getEmoji())
                .iconType(food.getIconType())
                .iconBgColor(food.getIconBgColor())
                .iconColor(food.getIconColor())
                .isHighlightedMore(food.getIsHighlightedMore())
                .isActive(food.getIsActive())
                .build();
    }
}
