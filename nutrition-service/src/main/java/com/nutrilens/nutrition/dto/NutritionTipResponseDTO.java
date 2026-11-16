package com.nutrilens.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTipResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String iconType;
    private String targetGoal;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
