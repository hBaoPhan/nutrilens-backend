package com.nutrilens.nutrition.service;

import com.nutrilens.nutrition.dto.CreateNutritionTipRequestDTO;
import com.nutrilens.nutrition.dto.NutritionTipResponseDTO;

import java.util.List;

public interface NutritionTipService {
    List<NutritionTipResponseDTO> getAllActiveTips(String targetGoal);
    NutritionTipResponseDTO getRandomTip();
    NutritionTipResponseDTO createTip(CreateNutritionTipRequestDTO request);
    NutritionTipResponseDTO getTipById(Long id);
    void deleteTip(Long id);
}
