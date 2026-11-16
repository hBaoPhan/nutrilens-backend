package com.nutrilens.nutrition.controller;

import com.nutrilens.common.dto.response.ApiResponse;
import com.nutrilens.nutrition.dto.CreateNutritionTipRequestDTO;
import com.nutrilens.nutrition.dto.NutritionTipResponseDTO;
import com.nutrilens.nutrition.service.NutritionTipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/nutrition/tips", "/api/v1/tips", "/api/tips"})
@RequiredArgsConstructor
@Slf4j
public class NutritionTipController {

    private final NutritionTipService nutritionTipService;

    @GetMapping
    public ResponseEntity<List<NutritionTipResponseDTO>> getTips(
            @RequestParam(required = false, defaultValue = "ALL") String targetGoal) {
        List<NutritionTipResponseDTO> tips = nutritionTipService.getAllActiveTips(targetGoal);
        return ResponseEntity.ok(tips);
    }

    @GetMapping("/random")
    public ResponseEntity<NutritionTipResponseDTO> getRandomTip() {
        NutritionTipResponseDTO tip = nutritionTipService.getRandomTip();
        return ResponseEntity.ok(tip);
    }

    @PostMapping
    public ResponseEntity<NutritionTipResponseDTO> createTip(@Valid @RequestBody CreateNutritionTipRequestDTO request) {
        NutritionTipResponseDTO tip = nutritionTipService.createTip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tip);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTip(@PathVariable Long id) {
        nutritionTipService.deleteTip(id);
        return ResponseEntity.noContent().build();
    }
}
