package com.nutrilens.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionResolutionDTO {
    private String foodCode;
    private String foodName;
    private Double densityGPerCm3;
    private Double rawVolumeCm3;
    private Double portionRatio;
    private Double effectiveVolumeCm3;
    private Double estimatedWeightG;
    private Double calculatedKcal;
    private Double proteinG;
    private Double carbG;
    private Double fatG;
    private Double confidenceScore;
    private MetBurnDTO metBurn;
}
