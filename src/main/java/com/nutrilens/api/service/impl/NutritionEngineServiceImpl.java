package com.nutrilens.api.service.impl;

import com.nutrilens.api.dto.request.MultiVolumetricNutritionRequestDTO;
import com.nutrilens.api.dto.request.VolumetricComponentItemDTO;
import com.nutrilens.api.dto.request.VolumetricNutritionRequestDTO;
import com.nutrilens.api.dto.response.ComponentNutritionResultDTO;
import com.nutrilens.api.dto.response.FoodItemResponseDTO;
import com.nutrilens.api.dto.response.MultiNutritionAnalysisResponseDTO;
import com.nutrilens.api.dto.response.NutritionAnalysisResponseDTO;
import com.nutrilens.api.entity.FoodDensity;
import com.nutrilens.api.exception.EntityNotFoundException;
import com.nutrilens.api.repository.FoodDensityRepository;
import com.nutrilens.api.service.NutritionEngineService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionEngineServiceImpl implements NutritionEngineService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NutritionEngineServiceImpl.class);

    private final FoodDensityRepository foodDensityRepository;

    public NutritionEngineServiceImpl(FoodDensityRepository foodDensityRepository) {
        this.foodDensityRepository = foodDensityRepository;
    }

    // Standard medical MET values
    private static final BigDecimal RUNNING_MET = BigDecimal.valueOf(8.3);   // ~8 km/h running
    private static final BigDecimal WALKING_MET = BigDecimal.valueOf(3.8);   // ~5 km/h brisk walking
    private static final BigDecimal OXYGEN_CONSUMPTION_FACTOR = BigDecimal.valueOf(3.5);
    private static final BigDecimal MET_DIVISOR = BigDecimal.valueOf(200);

    @PostConstruct
    public void seedInitialFoodDensities() {
        seedFoodItem("SALAD_EGG", "Salad Trứng Luộc Rau Mầm", "Breakfast",
                new BigDecimal("0.5556"), new BigDecimal("294.00"), new BigDecimal("25.00"),
                new BigDecimal("21.00"), new BigDecimal("14.00"), new BigDecimal("100.00"),
                new BigDecimal("180.00"), "🥗", "salad", "#FFEFE6", "#F37340", false);

        seedFoodItem("GREEN_VEG", "Green vegetable", "Breakfast",
                new BigDecimal("0.8333"), new BigDecimal("197.00"), new BigDecimal("13.50"),
                new BigDecimal("16.00"), new BigDecimal("21.00"), new BigDecimal("200.00"),
                new BigDecimal("240.00"), "🥦", "vegetable", "#E8F8ED", "#27AE60", true);

        seedFoodItem("PINEAPPLE", "Slice of Pineapple", "Breakfast",
                new BigDecimal("0.4762"), new BigDecimal("457.00"), new BigDecimal("84.00"),
                new BigDecimal("95.00"), new BigDecimal("72.00"), new BigDecimal("100.00"),
                new BigDecimal("210.00"), "🍍", "pineapple", "#FFF7E0", "#F2C94C", false);

        seedFoodItem("GREEK_YOGURT", "Sữa Chua Hy Lạp Trái Cây", "Breakfast",
                new BigDecimal("0.9375"), new BigDecimal("140.00"), new BigDecimal("12.00"),
                new BigDecimal("14.67"), new BigDecimal("3.33"), new BigDecimal("150.00"),
                new BigDecimal("160.00"), "🥣", "salad", "#FFEFE6", "#F37340", false);

        seedFoodItem("COM_TAM", "Cơm Tấm Sườn Bì Chả", "Lunch",
                new BigDecimal("1.0857"), new BigDecimal("171.05"), new BigDecimal("10.00"),
                new BigDecimal("18.95"), new BigDecimal("6.32"), new BigDecimal("380.00"),
                new BigDecimal("350.00"), "🍚", "salad", "#FFEBE0", "#D95933", false);

        seedFoodItem("BROKEN_RICE", "Cơm Tấm Sườn Bì Chả", "Lunch",
                new BigDecimal("1.0857"), new BigDecimal("171.05"), new BigDecimal("10.00"),
                new BigDecimal("18.95"), new BigDecimal("6.32"), new BigDecimal("380.00"),
                new BigDecimal("350.00"), "🍚", "salad", "#FFEBE0", "#D95933", false);

        seedFoodItem("CHICKEN_SALAD", "Salad Ức Gà Áp Chảo", "Lunch",
                new BigDecimal("0.8929"), new BigDecimal("168.00"), new BigDecimal("18.00"),
                new BigDecimal("6.00"), new BigDecimal("4.80"), new BigDecimal("250.00"),
                new BigDecimal("280.00"), "🥗", "salad", "#E8F8ED", "#27AE60", true);

        seedFoodItem("RICE_WHITE_COOKED", "Cơm trắng", "Lunch",
                new BigDecimal("0.8500"), new BigDecimal("130.00"), new BigDecimal("2.70"),
                new BigDecimal("28.20"), new BigDecimal("0.30"), new BigDecimal("200.00"),
                new BigDecimal("235.00"), "🍚", "salad", "#F4F6F8", "#4A5568", false);

        seedFoodItem("PHO_BO", "Phở Bò Tái Nạm", "Dinner",
                new BigDecimal("1.1250"), new BigDecimal("115.56"), new BigDecimal("7.11"),
                new BigDecimal("12.89"), new BigDecimal("3.56"), new BigDecimal("450.00"),
                new BigDecimal("400.00"), "🍜", "vegetable", "#EBF3FF", "#3380D9", false);

        seedFoodItem("BEEF_PHO", "Phở Bò Tái Nạm", "Dinner",
                new BigDecimal("1.1250"), new BigDecimal("115.56"), new BigDecimal("7.11"),
                new BigDecimal("12.89"), new BigDecimal("3.56"), new BigDecimal("450.00"),
                new BigDecimal("400.00"), "🍜", "vegetable", "#EBF3FF", "#3380D9", false);

        seedFoodItem("SALMON_STEAK", "Cá Hồi Nướng Măng Tây", "Dinner",
                new BigDecimal("1.1538"), new BigDecimal("193.33"), new BigDecimal("14.00"),
                new BigDecimal("2.67"), new BigDecimal("9.33"), new BigDecimal("300.00"),
                new BigDecimal("260.00"), "🥩", "salad", "#FFEFE6", "#F37340", true);

        // New diverse data additions for testing
        seedFoodItem("OATMEAL_BERRY", "Yến Mạch Việt Quất & Hạt Chia", "Breakfast",
                new BigDecimal("1.0476"), new BigDecimal("127.27"), new BigDecimal("5.45"),
                new BigDecimal("20.91"), new BigDecimal("2.73"), new BigDecimal("220.00"),
                new BigDecimal("210.00"), "🥣", "salad", "#FFEFE6", "#9B51E0", false);

        seedFoodItem("AVOCADO_TOAST", "Bánh Mì Bơ Trứng Chần", "Breakfast",
                new BigDecimal("0.8889"), new BigDecimal("225.00"), new BigDecimal("9.38"),
                new BigDecimal("18.75"), new BigDecimal("12.50"), new BigDecimal("160.00"),
                new BigDecimal("180.00"), "🥑", "salad", "#E8F8ED", "#27AE60", true);

        seedFoodItem("BUN_CHA_HA_NOI", "Bún Chả Hà Nội Nướng Than", "Lunch",
                new BigDecimal("1.0769"), new BigDecimal("128.57"), new BigDecimal("6.67"),
                new BigDecimal("16.19"), new BigDecimal("4.29"), new BigDecimal("420.00"),
                new BigDecimal("390.00"), "🍲", "vegetable", "#FFF7E0", "#D95933", false);

        seedFoodItem("BEEF_QUINOA_BOWL", "Cơm Quinoa Bò Áp Chảo", "Lunch",
                new BigDecimal("1.0323"), new BigDecimal("153.13"), new BigDecimal("11.25"),
                new BigDecimal("15.00"), new BigDecimal("5.00"), new BigDecimal("320.00"),
                new BigDecimal("310.00"), "🥗", "salad", "#FFEFE6", "#F37340", true);

        seedFoodItem("CANH_CHUA_CA_HOI", "Canh Chua Cá Hồi Nam Bộ", "Dinner",
                new BigDecimal("1.0526"), new BigDecimal("77.50"), new BigDecimal("6.50"),
                new BigDecimal("3.50"), new BigDecimal("4.00"), new BigDecimal("400.00"),
                new BigDecimal("380.00"), "🍲", "vegetable", "#EBF3FF", "#3380D9", false);

        seedFoodItem("GRILLED_CHICKEN_BREAST", "Ức Gà Nướng Rau Củ Cầu Vồng", "Dinner",
                new BigDecimal("0.9655"), new BigDecimal("135.71"), new BigDecimal("15.71"),
                new BigDecimal("6.43"), new BigDecimal("5.00"), new BigDecimal("280.00"),
                new BigDecimal("290.00"), "🍗", "salad", "#E8F8ED", "#27AE60", true);

        // Real-world Vision AI foods (COCO / Real Object Detection)
        seedFoodItem("APPLE", "Táo Đỏ Tươi", "Breakfast",
                new BigDecimal("0.8500"), new BigDecimal("52.00"), new BigDecimal("0.30"),
                new BigDecimal("13.80"), new BigDecimal("0.20"), new BigDecimal("182.00"),
                new BigDecimal("214.00"), "🍎", "pineapple", "#FFEFE6", "#EB5757", false);

        seedFoodItem("BANANA", "Chuối Chín", "Breakfast",
                new BigDecimal("0.9500"), new BigDecimal("89.00"), new BigDecimal("1.10"),
                new BigDecimal("22.80"), new BigDecimal("0.30"), new BigDecimal("120.00"),
                new BigDecimal("126.00"), "🍌", "pineapple", "#FFF7E0", "#F2C94C", false);

        seedFoodItem("ORANGE", "Cam Vàng Tươi", "Breakfast",
                new BigDecimal("0.9000"), new BigDecimal("47.00"), new BigDecimal("0.90"),
                new BigDecimal("11.80"), new BigDecimal("0.10"), new BigDecimal("150.00"),
                new BigDecimal("166.00"), "🍊", "pineapple", "#FFEFE6", "#F2994A", false);

        seedFoodItem("PIZZA", "Pizza Phô Mai & Nấm", "Lunch",
                new BigDecimal("0.8000"), new BigDecimal("266.00"), new BigDecimal("11.00"),
                new BigDecimal("33.00"), new BigDecimal("10.00"), new BigDecimal("200.00"),
                new BigDecimal("250.00"), "🍕", "salad", "#FFEBE0", "#D95933", false);

        // Atomic Ingredients / Common Components
        seedFoodItem("BOILED_EGG", "Trứng Gà Luộc", "Breakfast", "ATOMIC_INGREDIENT",
                new BigDecimal("1.0300"), new BigDecimal("155.00"), new BigDecimal("13.00"),
                new BigDecimal("1.10"), new BigDecimal("11.00"), new BigDecimal("50.00"),
                new BigDecimal("48.54"), "🥚", "egg", "#FFF9E6", "#F2994A", true);

        seedFoodItem("CARROT", "Cà Rốt Luộc / Tươi", "Lunch", "ATOMIC_INGREDIENT",
                new BigDecimal("0.8333"), new BigDecimal("41.00"), new BigDecimal("0.90"),
                new BigDecimal("9.60"), new BigDecimal("0.20"), new BigDecimal("100.00"),
                new BigDecimal("120.00"), "🥕", "vegetable", "#FFF0E6", "#EB5757", true);

        seedFoodItem("CHICKEN_BREAST", "Ức Gà Nướng / Luộc", "Dinner", "ATOMIC_INGREDIENT",
                new BigDecimal("0.9800"), new BigDecimal("165.00"), new BigDecimal("31.00"),
                new BigDecimal("0.00"), new BigDecimal("3.60"), new BigDecimal("150.00"),
                new BigDecimal("153.06"), "🍗", "meat", "#EBF5FB", "#2980B9", true);

        seedFoodItem("BROCCOLI", "Súp Lơ Xanh Luộc", "Lunch", "ATOMIC_INGREDIENT",
                new BigDecimal("0.5200"), new BigDecimal("34.00"), new BigDecimal("2.80"),
                new BigDecimal("6.60"), new BigDecimal("0.40"), new BigDecimal("150.00"),
                new BigDecimal("288.46"), "🥦", "vegetable", "#E8F8ED", "#27AE60", true);

        seedFoodItem("SWEET_POTATO", "Khoai Lang Luộc", "Breakfast", "ATOMIC_INGREDIENT",
                new BigDecimal("0.9500"), new BigDecimal("86.00"), new BigDecimal("1.60"),
                new BigDecimal("20.10"), new BigDecimal("0.10"), new BigDecimal("150.00"),
                new BigDecimal("157.89"), "🍠", "vegetable", "#FDF2E9", "#E67E22", false);

        seedFoodItem("TOFU_STEAMED", "Đậu Phụ Trắng Luộc", "Lunch", "ATOMIC_INGREDIENT",
                new BigDecimal("0.9600"), new BigDecimal("76.00"), new BigDecimal("8.00"),
                new BigDecimal("1.90"), new BigDecimal("4.80"), new BigDecimal("150.00"),
                new BigDecimal("156.25"), "🧈", "salad", "#F4F6F7", "#7F8C8D", false);

        seedFoodItem("SHRIMP_BOILED", "Tôm Luộc Bóc Vỏ", "Dinner", "ATOMIC_INGREDIENT",
                new BigDecimal("1.0200"), new BigDecimal("99.00"), new BigDecimal("24.00"),
                new BigDecimal("0.20"), new BigDecimal("0.30"), new BigDecimal("100.00"),
                new BigDecimal("98.04"), "🦐", "salad", "#FDEDEC", "#E74C3C", false);

        seedFoodItem("CUCUMBER", "Dưa Chuột Tươi", "Lunch", "ATOMIC_INGREDIENT",
                new BigDecimal("0.6800"), new BigDecimal("15.00"), new BigDecimal("0.70"),
                new BigDecimal("3.60"), new BigDecimal("0.10"), new BigDecimal("100.00"),
                new BigDecimal("147.06"), "🥒", "vegetable", "#E8F8ED", "#27AE60", false);

        seedFoodItem("RICE_WHITE", "Cơm Trắng Nấu Chín", "Lunch", "ATOMIC_INGREDIENT",
                new BigDecimal("0.7500"), new BigDecimal("130.00"), new BigDecimal("2.70"),
                new BigDecimal("28.20"), new BigDecimal("0.30"), new BigDecimal("200.00"),
                new BigDecimal("266.67"), "🍚", "grain", "#F8F9F9", "#566573", false);

        seedFoodItem("COCONUT", "Dừa Tươi / Cùi Dừa", "Breakfast", "ATOMIC_INGREDIENT",
                new BigDecimal("0.9800"), new BigDecimal("354.00"), new BigDecimal("3.30"),
                new BigDecimal("15.20"), new BigDecimal("33.50"), new BigDecimal("100.00"),
                new BigDecimal("102.04"), "🥥", "salad", "#F5EEF8", "#8E44AD", false);

        seedFoodItem("SUA_CHUA", "Sữa Chua Tự Nhiên", "Breakfast", "ATOMIC_INGREDIENT",
                new BigDecimal("1.0300"), new BigDecimal("61.00"), new BigDecimal("3.50"),
                new BigDecimal("4.70"), new BigDecimal("3.30"), new BigDecimal("100.00"),
                new BigDecimal("97.09"), "🥛", "salad", "#EBF5FB", "#3498DB", false);

        seedFoodItem("WATERMELON", "Dưa Hấu Đỏ Tươi", "Breakfast", "ATOMIC_INGREDIENT",
                new BigDecimal("0.9600"), new BigDecimal("30.00"), new BigDecimal("0.60"),
                new BigDecimal("7.60"), new BigDecimal("0.20"), new BigDecimal("250.00"),
                new BigDecimal("260.42"), "🍉", "salad", "#FDEDEC", "#E74C3C", false);

        log.info("Initialized NutriLens Food Database seeds (34 food items across Composite Dishes & Atomic Ingredients).");
    }

    private void seedFoodItem(String foodCode, String displayName, String category,
                              BigDecimal density, BigDecimal kcal100g, BigDecimal p100g,
                              BigDecimal c100g, BigDecimal f100g, BigDecimal servingWeightG,
                              BigDecimal servingVolumeCm3, String emoji, String iconType,
                              String iconBgColor, String iconColor, boolean isHighlightedMore) {
        seedFoodItem(foodCode, displayName, category, "COMPOSITE_DISH", density, kcal100g, p100g,
                c100g, f100g, servingWeightG, servingVolumeCm3, emoji, iconType, iconBgColor, iconColor, isHighlightedMore);
    }

    private void seedFoodItem(String foodCode, String displayName, String category, String foodType,
                              BigDecimal density, BigDecimal kcal100g, BigDecimal p100g,
                              BigDecimal c100g, BigDecimal f100g, BigDecimal servingWeightG,
                              BigDecimal servingVolumeCm3, String emoji, String iconType,
                              String iconBgColor, String iconColor, boolean isHighlightedMore) {
        FoodDensity food = foodDensityRepository.findByFoodCode(foodCode).orElse(null);
        if (food == null) {
            food = FoodDensity.builder()
                    .foodCode(foodCode)
                    .displayName(displayName)
                    .category(category)
                    .foodType(foodType)
                    .densityGramsPerCm3(density)
                    .kcalPer100g(kcal100g)
                    .proteinPer100g(p100g)
                    .carbPer100g(c100g)
                    .fatPer100g(f100g)
                    .servingWeightG(servingWeightG)
                    .servingVolumeCm3(servingVolumeCm3)
                    .emoji(emoji)
                    .iconType(iconType)
                    .iconBgColor(iconBgColor)
                    .iconColor(iconColor)
                    .isHighlightedMore(isHighlightedMore)
                    .isActive(true)
                    .build();
            foodDensityRepository.save(food);
        } else {
            // Update fields if already exists
            food.setDisplayName(displayName);
            food.setCategory(category);
            food.setFoodType(foodType);
            food.setDensityGramsPerCm3(density);
            food.setKcalPer100g(kcal100g);
            food.setProteinPer100g(p100g);
            food.setCarbPer100g(c100g);
            food.setFatPer100g(f100g);
            food.setServingWeightG(servingWeightG);
            food.setServingVolumeCm3(servingVolumeCm3);
            food.setEmoji(emoji);
            food.setIconType(iconType);
            food.setIconBgColor(iconBgColor);
            food.setIconColor(iconColor);
            food.setIsHighlightedMore(isHighlightedMore);
            food.setIsActive(true);
            foodDensityRepository.save(food);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponseDTO> getActiveFoods(String category) {
        List<FoodDensity> foods;
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            foods = foodDensityRepository.findAllByCategoryIgnoreCaseAndIsActiveTrue(category.trim());
        } else {
            foods = foodDensityRepository.findAllByIsActiveTrue();
        }

        return foods.stream().map(this::mapToDTO).collect(Collectors.toList());
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
                .category(food.getCategory() != null ? food.getCategory() : "Breakfast")
                .foodType(food.getFoodType() != null ? food.getFoodType() : "COMPOSITE_DISH")
                .kcal(totalKcal)
                .weightG(weight)
                .volumeCm3(food.getServingVolumeCm3() != null ? food.getServingVolumeCm3() : BigDecimal.valueOf(150))
                .proteinG(totalProtein)
                .carbsG(totalCarbs)
                .fatG(totalFat)
                .emoji(food.getEmoji() != null ? food.getEmoji() : "🥗")
                .iconType(food.getIconType() != null ? food.getIconType() : "salad")
                .iconBgColor(food.getIconBgColor() != null ? food.getIconBgColor() : "#FFEFE6")
                .iconColor(food.getIconColor() != null ? food.getIconColor() : "#F37340")
                .isHighlightedMore(Boolean.TRUE.equals(food.getIsHighlightedMore()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionAnalysisResponseDTO analyzeVolumetricPortion(VolumetricNutritionRequestDTO request) {
        FoodDensity food = foodDensityRepository.findByFoodCodeAndIsActiveTrue(request.getFoodCode())
                .orElseThrow(() -> new EntityNotFoundException("Food density record not found for: " + request.getFoodCode()));

        // 1. Effective Volume: V_eff = V_scanned * portionRatio
        BigDecimal effectiveVolume = request.getVolumeCm3()
                .multiply(request.getPortionRatio())
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Estimated Weight: m = V_eff * density (g/cm3)
        BigDecimal estimatedWeightGrams = effectiveVolume
                .multiply(food.getDensityGramsPerCm3())
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Macronutrient conversion ratio per 100g
        BigDecimal factorPerHundred = estimatedWeightGrams.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal totalKcal = food.getKcalPer100g().multiply(factorPerHundred).setScale(1, RoundingMode.HALF_UP);
        BigDecimal protein = food.getProteinPer100g().multiply(factorPerHundred).setScale(2, RoundingMode.HALF_UP);
        BigDecimal carb = food.getCarbPer100g().multiply(factorPerHundred).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fat = food.getFatPer100g().multiply(factorPerHundred).setScale(2, RoundingMode.HALF_UP);

        // 4. Burn Visualizer calculation via MET formula
        int runningMinutes = computeExerciseDuration(totalKcal, RUNNING_MET, request.getUserWeightKg());
        int walkingMinutes = computeExerciseDuration(totalKcal, WALKING_MET, request.getUserWeightKg());

        return NutritionAnalysisResponseDTO.builder()
                .foodName(food.getDisplayName())
                .effectiveVolumeCm3(effectiveVolume)
                .estimatedWeightGrams(estimatedWeightGrams)
                .totalCaloriesKcal(totalKcal)
                .proteinGrams(protein)
                .carbGrams(carb)
                .fatGrams(fat)
                .runningMinutesNeeded(runningMinutes)
                .briskWalkingMinutesNeeded(walkingMinutes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MultiNutritionAnalysisResponseDTO analyzeMultiVolumetricPortion(MultiVolumetricNutritionRequestDTO request) {
        BigDecimal totalEffectiveVolume = BigDecimal.ZERO;
        BigDecimal totalEstimatedWeight = BigDecimal.ZERO;
        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarb = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;

        List<ComponentNutritionResultDTO> componentResults = new ArrayList<>();

        for (VolumetricComponentItemDTO comp : request.getComponents()) {
            FoodDensity food = foodDensityRepository.findByFoodCodeAndIsActiveTrue(comp.getFoodCode())
                    .orElseThrow(() -> new EntityNotFoundException("Food density record not found for component code: " + comp.getFoodCode()));

            BigDecimal effectiveVolume = comp.getVolumeCm3()
                    .multiply(comp.getPortionRatio() != null ? comp.getPortionRatio() : BigDecimal.ONE)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal estimatedWeightGrams = effectiveVolume
                    .multiply(food.getDensityGramsPerCm3())
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal factorPerHundred = estimatedWeightGrams.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            BigDecimal kcal = food.getKcalPer100g().multiply(factorPerHundred).setScale(1, RoundingMode.HALF_UP);
            BigDecimal protein = food.getProteinPer100g().multiply(factorPerHundred).setScale(2, RoundingMode.HALF_UP);
            BigDecimal carb = food.getCarbPer100g().multiply(factorPerHundred).setScale(2, RoundingMode.HALF_UP);
            BigDecimal fat = food.getFatPer100g().multiply(factorPerHundred).setScale(2, RoundingMode.HALF_UP);

            totalEffectiveVolume = totalEffectiveVolume.add(effectiveVolume);
            totalEstimatedWeight = totalEstimatedWeight.add(estimatedWeightGrams);
            totalCalories = totalCalories.add(kcal);
            totalProtein = totalProtein.add(protein);
            totalCarb = totalCarb.add(carb);
            totalFat = totalFat.add(fat);

            componentResults.add(ComponentNutritionResultDTO.builder()
                    .foodCode(food.getFoodCode())
                    .displayName(food.getDisplayName())
                    .foodType(food.getFoodType() != null ? food.getFoodType() : "ATOMIC_INGREDIENT")
                    .emoji(food.getEmoji())
                    .effectiveVolumeCm3(effectiveVolume)
                    .estimatedWeightGrams(estimatedWeightGrams)
                    .caloriesKcal(kcal)
                    .proteinGrams(protein)
                    .carbGrams(carb)
                    .fatGrams(fat)
                    .build());
        }

        int runningMinutes = computeExerciseDuration(totalCalories, RUNNING_MET, request.getUserWeightKg());
        int walkingMinutes = computeExerciseDuration(totalCalories, WALKING_MET, request.getUserWeightKg());

        // Generate clean summary title for identical (e.g. 2x Boiled Egg) or mixed components
        Map<String, Long> codeFrequencies = request.getComponents().stream()
                .collect(Collectors.groupingBy(VolumetricComponentItemDTO::getFoodCode, Collectors.counting()));

        String summaryTitle;
        if (codeFrequencies.size() == 1) {
            String singleCode = codeFrequencies.keySet().iterator().next();
            long count = codeFrequencies.get(singleCode);
            String dName = componentResults.isEmpty() ? singleCode : componentResults.get(0).getDisplayName();
            summaryTitle = (count > 1 ? count + "x " : "") + dName;
        } else {
            summaryTitle = "Đĩa đa thành phần (" + componentResults.size() + " món)";
        }

        return MultiNutritionAnalysisResponseDTO.builder()
                .summaryTitle(summaryTitle)
                .totalComponentsCount(componentResults.size())
                .totalEffectiveVolumeCm3(totalEffectiveVolume)
                .totalEstimatedWeightGrams(totalEstimatedWeight)
                .totalCaloriesKcal(totalCalories)
                .totalProteinGrams(totalProtein)
                .totalCarbGrams(totalCarb)
                .totalFatGrams(totalFat)
                .runningMinutesNeeded(runningMinutes)
                .briskWalkingMinutesNeeded(walkingMinutes)
                .components(componentResults)
                .build();
    }

    private int computeExerciseDuration(BigDecimal totalKcal, BigDecimal met, BigDecimal userWeightKg) {
        // Cal/min = (MET * 3.5 * Weight) / 200
        BigDecimal calPerMinute = met.multiply(OXYGEN_CONSUMPTION_FACTOR).multiply(userWeightKg)
                .divide(MET_DIVISOR, 4, RoundingMode.HALF_UP);

        if (calPerMinute.compareTo(BigDecimal.ZERO) <= 0) return 0;

        return totalKcal.divide(calPerMinute, 0, RoundingMode.CEILING).intValue();
    }
}
