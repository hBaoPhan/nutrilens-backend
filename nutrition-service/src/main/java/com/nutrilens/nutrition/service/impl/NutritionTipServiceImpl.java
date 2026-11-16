package com.nutrilens.nutrition.service.impl;

import com.nutrilens.common.exception.EntityNotFoundException;
import com.nutrilens.nutrition.dto.CreateNutritionTipRequestDTO;
import com.nutrilens.nutrition.dto.NutritionTipResponseDTO;
import com.nutrilens.nutrition.entity.NutritionTip;
import com.nutrilens.nutrition.repository.NutritionTipRepository;
import com.nutrilens.nutrition.service.NutritionTipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionTipServiceImpl implements NutritionTipService {

    private final NutritionTipRepository nutritionTipRepository;
    private final Random random = new Random();

    @Override
    @Transactional(readOnly = true)
    public List<NutritionTipResponseDTO> getAllActiveTips(String targetGoal) {
        List<NutritionTip> tips = (targetGoal != null && !targetGoal.isBlank() && !targetGoal.equalsIgnoreCase("ALL"))
                ? nutritionTipRepository.findAllByTargetGoalIgnoreCaseAndIsActiveTrue(targetGoal.trim())
                : nutritionTipRepository.findAllByIsActiveTrue();

        return tips.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionTipResponseDTO getRandomTip() {
        List<NutritionTip> tips = nutritionTipRepository.findAllByIsActiveTrue();
        if (tips.isEmpty()) {
            throw new EntityNotFoundException("No active nutrition tips found");
        }
        NutritionTip randomTip = tips.get(random.nextInt(tips.size()));
        return mapToDTO(randomTip);
    }

    @Override
    @Transactional
    public NutritionTipResponseDTO createTip(CreateNutritionTipRequestDTO request) {
        NutritionTip tip = NutritionTip.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .category(request.getCategory().trim().toUpperCase())
                .iconType(request.getIconType() != null ? request.getIconType() : "salad")
                .targetGoal(request.getTargetGoal() != null ? request.getTargetGoal().trim().toUpperCase() : "ALL")
                .isActive(true)
                .build();

        NutritionTip saved = nutritionTipRepository.save(tip);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionTipResponseDTO getTipById(Long id) {
        NutritionTip tip = nutritionTipRepository.findById(id)
                .filter(NutritionTip::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("Nutrition tip not found: " + id));
        return mapToDTO(tip);
    }

    @Override
    @Transactional
    public void deleteTip(Long id) {
        NutritionTip tip = nutritionTipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nutrition tip not found: " + id));
        tip.setIsActive(false);
        nutritionTipRepository.save(tip);
    }

    private NutritionTipResponseDTO mapToDTO(NutritionTip tip) {
        return NutritionTipResponseDTO.builder()
                .id(tip.getId())
                .title(tip.getTitle())
                .content(tip.getContent())
                .category(tip.getCategory())
                .iconType(tip.getIconType())
                .targetGoal(tip.getTargetGoal())
                .isActive(tip.getIsActive())
                .createdAt(tip.getCreatedAt())
                .build();
    }
}
