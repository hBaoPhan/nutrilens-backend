package com.nutrilens.api.service;

import com.nutrilens.api.dto.request.VolumetricNutritionRequestDTO;
import com.nutrilens.api.dto.response.NutritionAnalysisResponseDTO;
import com.nutrilens.api.entity.FoodDensity;
import com.nutrilens.api.exception.EntityNotFoundException;
import com.nutrilens.api.repository.FoodDensityRepository;
import com.nutrilens.api.service.impl.NutritionEngineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nutrilens.api.dto.response.FoodItemResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionEngineServiceTest {

    @Mock
    private FoodDensityRepository foodDensityRepository;

    @InjectMocks
    private NutritionEngineServiceImpl nutritionEngineService;

    private FoodDensity whiteRice;

    @BeforeEach
    void setUp() {
        whiteRice = FoodDensity.builder()
                .id(1L)
                .foodCode("RICE_WHITE_COOKED")
                .displayName("Cơm trắng")
                .densityGramsPerCm3(new BigDecimal("0.8500"))
                .kcalPer100g(new BigDecimal("130.00"))
                .proteinPer100g(new BigDecimal("2.70"))
                .carbPer100g(new BigDecimal("28.20"))
                .fatPer100g(new BigDecimal("0.30"))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should accurately calculate calories and weight for 100% portion")
    void testAccurateCalculationFullPortion() {
        when(foodDensityRepository.findByFoodCodeAndIsActiveTrue("RICE_WHITE_COOKED"))
                .thenReturn(Optional.of(whiteRice));

        VolumetricNutritionRequestDTO request = VolumetricNutritionRequestDTO.builder()
                .foodCode("RICE_WHITE_COOKED")
                .volumeCm3(new BigDecimal("200.00")) // 200 cm3
                .portionRatio(BigDecimal.ONE)         // 100%
                .userWeightKg(new BigDecimal("65.0"))
                .build();

        NutritionAnalysisResponseDTO result = nutritionEngineService.analyzeVolumetricPortion(request);

        assertNotNull(result);
        assertEquals("Cơm trắng", result.getFoodName());
        // Estimated weight = 200 * 0.85 = 170.00g
        assertEquals(new BigDecimal("170.00"), result.getEstimatedWeightGrams());
        // Kcal = 130 * (170 / 100) = 221.0 kcal
        assertEquals(new BigDecimal("221.0"), result.getTotalCaloriesKcal());
        // Running minutes needed > 0
        assertTrue(result.getRunningMinutesNeeded() > 0);
        assertTrue(result.getBriskWalkingMinutesNeeded() > result.getRunningMinutesNeeded());
    }

    @Test
    @DisplayName("Should adjust calories proportionally when portion ratio is reduced to 50%")
    void testPortionReduction() {
        when(foodDensityRepository.findByFoodCodeAndIsActiveTrue("RICE_WHITE_COOKED"))
                .thenReturn(Optional.of(whiteRice));

        VolumetricNutritionRequestDTO request = VolumetricNutritionRequestDTO.builder()
                .foodCode("RICE_WHITE_COOKED")
                .volumeCm3(new BigDecimal("200.00"))
                .portionRatio(new BigDecimal("0.50")) // Reduced by half
                .userWeightKg(new BigDecimal("65.0"))
                .build();

        NutritionAnalysisResponseDTO result = nutritionEngineService.analyzeVolumetricPortion(request);

        assertNotNull(result);
        // Effective volume = 200 * 0.50 = 100.00 cm3
        assertEquals(new BigDecimal("100.00"), result.getEffectiveVolumeCm3());
        // Estimated weight = 100 * 0.85 = 85.00g
        assertEquals(new BigDecimal("85.00"), result.getEstimatedWeightGrams());
        // Kcal = 130 * 0.85 = 110.5 kcal
        assertEquals(new BigDecimal("110.5"), result.getTotalCaloriesKcal());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when food code is unknown")
    void testUnknownFoodCodeThrowsException() {
        when(foodDensityRepository.findByFoodCodeAndIsActiveTrue("UNKNOWN_FOOD"))
                .thenReturn(Optional.empty());

        VolumetricNutritionRequestDTO request = VolumetricNutritionRequestDTO.builder()
                .foodCode("UNKNOWN_FOOD")
                .volumeCm3(new BigDecimal("150.00"))
                .portionRatio(BigDecimal.ONE)
                .userWeightKg(new BigDecimal("70.0"))
                .build();

        assertThrows(EntityNotFoundException.class, () -> nutritionEngineService.analyzeVolumetricPortion(request));
    }

    @Test
    @DisplayName("Should return active foods mapped to DTOs for a given category")
    void testGetActiveFoods() {
        FoodDensity salad = FoodDensity.builder()
                .id(2L)
                .foodCode("SALAD_EGG")
                .displayName("Salad with egg")
                .category("Breakfast")
                .densityGramsPerCm3(new BigDecimal("0.5556"))
                .kcalPer100g(new BigDecimal("294.00"))
                .proteinPer100g(new BigDecimal("25.00"))
                .carbPer100g(new BigDecimal("21.00"))
                .fatPer100g(new BigDecimal("14.00"))
                .servingWeightG(new BigDecimal("100.00"))
                .servingVolumeCm3(new BigDecimal("180.00"))
                .emoji("🥗")
                .iconType("salad")
                .iconBgColor("#FFEFE6")
                .iconColor("#F37340")
                .isHighlightedMore(false)
                .isActive(true)
                .build();

        when(foodDensityRepository.findAllByCategoryIgnoreCaseAndIsActiveTrue("Breakfast"))
                .thenReturn(List.of(salad));

        List<FoodItemResponseDTO> foods = nutritionEngineService.getActiveFoods("Breakfast");

        assertNotNull(foods);
        assertEquals(1, foods.size());
        FoodItemResponseDTO dto = foods.get(0);
        assertEquals("SALAD_EGG", dto.getFoodCode());
        assertEquals("Salad with egg", dto.getName());
        assertEquals("Breakfast", dto.getCategory());
        assertEquals(new BigDecimal("294"), dto.getKcal());
    }

    @Test
    @DisplayName("Should detect and summarize multiple instances of the same food (e.g. 2 Boiled Eggs)")
    void testAnalyzeMultiVolumetricTwoBoiledEggs() {
        FoodDensity boiledEgg = FoodDensity.builder()
                .id(14L)
                .foodCode("BOILED_EGG")
                .displayName("Trứng Gà Luộc")
                .densityGramsPerCm3(new BigDecimal("1.0300"))
                .kcalPer100g(new BigDecimal("155.00"))
                .proteinPer100g(new BigDecimal("13.00"))
                .carbPer100g(new BigDecimal("1.10"))
                .fatPer100g(new BigDecimal("11.00"))
                .isActive(true)
                .build();

        when(foodDensityRepository.findByFoodCodeAndIsActiveTrue("BOILED_EGG"))
                .thenReturn(Optional.of(boiledEgg));

        com.nutrilens.api.dto.request.VolumetricComponentItemDTO egg1 = com.nutrilens.api.dto.request.VolumetricComponentItemDTO.builder()
                .foodCode("BOILED_EGG")
                .volumeCm3(new BigDecimal("48.54")) // 50g per egg
                .portionRatio(BigDecimal.ONE)
                .build();

        com.nutrilens.api.dto.request.VolumetricComponentItemDTO egg2 = com.nutrilens.api.dto.request.VolumetricComponentItemDTO.builder()
                .foodCode("BOILED_EGG")
                .volumeCm3(new BigDecimal("48.54"))
                .portionRatio(BigDecimal.ONE)
                .build();

        com.nutrilens.api.dto.request.MultiVolumetricNutritionRequestDTO req = com.nutrilens.api.dto.request.MultiVolumetricNutritionRequestDTO.builder()
                .components(List.of(egg1, egg2))
                .userWeightKg(new BigDecimal("65.0"))
                .build();

        com.nutrilens.api.dto.response.MultiNutritionAnalysisResponseDTO res = nutritionEngineService.analyzeMultiVolumetricPortion(req);

        assertNotNull(res);
        assertEquals(2, res.getTotalComponentsCount());
        assertEquals("2x Trứng Gà Luộc", res.getSummaryTitle());
        assertEquals(new BigDecimal("100.00"), res.getTotalEstimatedWeightGrams());
        assertEquals(new BigDecimal("155.0"), res.getTotalCaloriesKcal());
        assertEquals(new BigDecimal("13.00"), res.getTotalProteinGrams());
    }
}
