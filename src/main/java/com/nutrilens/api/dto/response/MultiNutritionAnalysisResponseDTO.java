package com.nutrilens.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiNutritionAnalysisResponseDTO {

    private String summaryTitle;
    private Integer totalComponentsCount;
    private BigDecimal totalEffectiveVolumeCm3;
    private BigDecimal totalEstimatedWeightGrams;
    private BigDecimal totalCaloriesKcal;
    private BigDecimal totalProteinGrams;
    private BigDecimal totalCarbGrams;
    private BigDecimal totalFatGrams;
    private Integer runningMinutesNeeded;
    private Integer briskWalkingMinutesNeeded;
    private List<ComponentNutritionResultDTO> components;

    public String getSummaryTitle() { return summaryTitle; }
    public void setSummaryTitle(String summaryTitle) { this.summaryTitle = summaryTitle; }
    public Integer getTotalComponentsCount() { return totalComponentsCount; }
    public void setTotalComponentsCount(Integer totalComponentsCount) { this.totalComponentsCount = totalComponentsCount; }
    public BigDecimal getTotalEffectiveVolumeCm3() { return totalEffectiveVolumeCm3; }
    public void setTotalEffectiveVolumeCm3(BigDecimal totalEffectiveVolumeCm3) { this.totalEffectiveVolumeCm3 = totalEffectiveVolumeCm3; }
    public BigDecimal getTotalEstimatedWeightGrams() { return totalEstimatedWeightGrams; }
    public void setTotalEstimatedWeightGrams(BigDecimal totalEstimatedWeightGrams) { this.totalEstimatedWeightGrams = totalEstimatedWeightGrams; }
    public BigDecimal getTotalCaloriesKcal() { return totalCaloriesKcal; }
    public void setTotalCaloriesKcal(BigDecimal totalCaloriesKcal) { this.totalCaloriesKcal = totalCaloriesKcal; }
    public BigDecimal getTotalProteinGrams() { return totalProteinGrams; }
    public void setTotalProteinGrams(BigDecimal totalProteinGrams) { this.totalProteinGrams = totalProteinGrams; }
    public BigDecimal getTotalCarbGrams() { return totalCarbGrams; }
    public void setTotalCarbGrams(BigDecimal totalCarbGrams) { this.totalCarbGrams = totalCarbGrams; }
    public BigDecimal getTotalFatGrams() { return totalFatGrams; }
    public void setTotalFatGrams(BigDecimal totalFatGrams) { this.totalFatGrams = totalFatGrams; }
    public Integer getRunningMinutesNeeded() { return runningMinutesNeeded; }
    public void setRunningMinutesNeeded(Integer runningMinutesNeeded) { this.runningMinutesNeeded = runningMinutesNeeded; }
    public Integer getBriskWalkingMinutesNeeded() { return briskWalkingMinutesNeeded; }
    public void setBriskWalkingMinutesNeeded(Integer briskWalkingMinutesNeeded) { this.briskWalkingMinutesNeeded = briskWalkingMinutesNeeded; }
    public List<ComponentNutritionResultDTO> getComponents() { return components; }
    public void setComponents(List<ComponentNutritionResultDTO> components) { this.components = components; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String summaryTitle;
        private Integer totalComponentsCount;
        private BigDecimal totalEffectiveVolumeCm3;
        private BigDecimal totalEstimatedWeightGrams;
        private BigDecimal totalCaloriesKcal;
        private BigDecimal totalProteinGrams;
        private BigDecimal totalCarbGrams;
        private BigDecimal totalFatGrams;
        private Integer runningMinutesNeeded;
        private Integer briskWalkingMinutesNeeded;
        private List<ComponentNutritionResultDTO> components;

        public Builder summaryTitle(String summaryTitle) { this.summaryTitle = summaryTitle; return this; }
        public Builder totalComponentsCount(Integer totalComponentsCount) { this.totalComponentsCount = totalComponentsCount; return this; }
        public Builder totalEffectiveVolumeCm3(BigDecimal totalEffectiveVolumeCm3) { this.totalEffectiveVolumeCm3 = totalEffectiveVolumeCm3; return this; }
        public Builder totalEstimatedWeightGrams(BigDecimal totalEstimatedWeightGrams) { this.totalEstimatedWeightGrams = totalEstimatedWeightGrams; return this; }
        public Builder totalCaloriesKcal(BigDecimal totalCaloriesKcal) { this.totalCaloriesKcal = totalCaloriesKcal; return this; }
        public Builder totalProteinGrams(BigDecimal totalProteinGrams) { this.totalProteinGrams = totalProteinGrams; return this; }
        public Builder totalCarbGrams(BigDecimal totalCarbGrams) { this.totalCarbGrams = totalCarbGrams; return this; }
        public Builder totalFatGrams(BigDecimal totalFatGrams) { this.totalFatGrams = totalFatGrams; return this; }
        public Builder runningMinutesNeeded(Integer runningMinutesNeeded) { this.runningMinutesNeeded = runningMinutesNeeded; return this; }
        public Builder briskWalkingMinutesNeeded(Integer briskWalkingMinutesNeeded) { this.briskWalkingMinutesNeeded = briskWalkingMinutesNeeded; return this; }
        public Builder components(List<ComponentNutritionResultDTO> components) { this.components = components; return this; }

        public MultiNutritionAnalysisResponseDTO build() {
            MultiNutritionAnalysisResponseDTO dto = new MultiNutritionAnalysisResponseDTO();
            dto.summaryTitle = this.summaryTitle;
            dto.totalComponentsCount = this.totalComponentsCount;
            dto.totalEffectiveVolumeCm3 = this.totalEffectiveVolumeCm3;
            dto.totalEstimatedWeightGrams = this.totalEstimatedWeightGrams;
            dto.totalCaloriesKcal = this.totalCaloriesKcal;
            dto.totalProteinGrams = this.totalProteinGrams;
            dto.totalCarbGrams = this.totalCarbGrams;
            dto.totalFatGrams = this.totalFatGrams;
            dto.runningMinutesNeeded = this.runningMinutesNeeded;
            dto.briskWalkingMinutesNeeded = this.briskWalkingMinutesNeeded;
            dto.components = this.components;
            return dto;
        }
    }
}
