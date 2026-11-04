package com.nutrilens.api.controller;

import com.nutrilens.api.dto.response.MealRecommendationResponseDTO;
import com.nutrilens.api.service.MealRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nutrition/recommendations")
@RequiredArgsConstructor
@Slf4j
public class MealRecommendationController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MealRecommendationController.class);

    private final MealRecommendationService mealRecommendationService;

    public MealRecommendationController(MealRecommendationService mealRecommendationService) {
        this.mealRecommendationService = mealRecommendationService;
    }

    @GetMapping("/next-meal")
    public ResponseEntity<MealRecommendationResponseDTO> getNextMealRecommendation(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            Authentication authentication) {

        String targetUsername = username;
        if ((targetUsername == null || targetUsername.trim().isEmpty()) && authentication != null && authentication.isAuthenticated()) {
            targetUsername = authentication.getName();
        }

        log.info("API GET /api/v1/nutrition/recommendations/next-meal invoked for userId={}, username={}", userId, targetUsername);
        MealRecommendationResponseDTO recommendation = mealRecommendationService.getNextMealRecommendation(userId, targetUsername);
        return ResponseEntity.ok(recommendation);
    }
}
