package com.nutrilens.recommendation.controller;

import com.nutrilens.recommendation.dto.MealRecommendationResponseDTO;
import com.nutrilens.recommendation.service.MealRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final MealRecommendationService mealRecommendationService;

    @GetMapping({"/api/recommendations/next-meal", "/api/v1/recommendations/next-meal"})
    public ResponseEntity<MealRecommendationResponseDTO> getNextMealRecommendation(
            @RequestParam(required = false) String username,
            @RequestHeader(value = "X-User-Username", required = false) String authUser) {

        String targetUser = (username != null && !username.isBlank()) ? username : authUser;
        if (targetUser == null || targetUser.isBlank()) {
            targetUser = "baophan";
        }

        log.info("[RECOMMENDATION CONTROLLER] Request for next meal recommendation: user={}", targetUser);
        MealRecommendationResponseDTO recommendation = mealRecommendationService.getNextMealRecommendation(targetUser);
        return ResponseEntity.ok(recommendation);
    }
}
