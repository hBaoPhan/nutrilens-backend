package com.nutrilens.api.controller;

import com.nutrilens.api.config.SecurityConfig;
import com.nutrilens.api.dto.response.MealRecommendationResponseDTO;
import com.nutrilens.api.dto.response.RecommendedDishItemDTO;
import com.nutrilens.api.service.MealRecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealRecommendationController.class)
@Import(SecurityConfig.class)
class MealRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealRecommendationService mealRecommendationService;

    @Test
    @DisplayName("Should return 200 OK with meal recommendation response")
    void testGetNextMealRecommendation() throws Exception {
        RecommendedDishItemDTO mockDish = RecommendedDishItemDTO.builder()
                .foodCode("GRILLED_CHICKEN_BREAST")
                .displayName("Ức Gà Nướng")
                .category("Dinner")
                .estimatedKcal(new BigDecimal("380.0"))
                .proteinG(new BigDecimal("43.90"))
                .carbG(new BigDecimal("18.00"))
                .fatG(new BigDecimal("14.00"))
                .matchScore(new BigDecimal("0.92"))
                .suitabilityNote("Cung cấp 380 kcal, 43.9g protein")
                .build();

        MealRecommendationResponseDTO mockResponse = MealRecommendationResponseDTO.builder()
                .userId(2L)
                .username("user1")
                .targetMealType("DINNER")
                .fitnessGoal("MAINTAIN")
                .dailyCalorieGoal(2000)
                .todayConsumedKcal(new BigDecimal("1200.0"))
                .remainingKcalToday(new BigDecimal("800.0"))
                .targetMealKcal(new BigDecimal("600.0"))
                .targetMealProteinG(new BigDecimal("28.0"))
                .aiReasoningSummary("Bữa tối giàu đạm hỗ trợ hồi phục cơ bắp và kiểm soát calo.")
                .recommendationSource("AI_RAG_GEMINI")
                .recommendedDishes(List.of(mockDish))
                .build();

        when(mealRecommendationService.getNextMealRecommendation(any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/nutrition/recommendations/next-meal")
                        .param("username", "user1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.targetMealType").value("DINNER"))
                .andExpect(jsonPath("$.remainingKcalToday").value(800.0))
                .andExpect(jsonPath("$.recommendedDishes[0].foodCode").value("GRILLED_CHICKEN_BREAST"))
                .andExpect(jsonPath("$.aiReasoningSummary").exists());
    }
}
