package com.nutrilens.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedDishItemDTO {
    private String foodCode;
    private String displayName;
    private String category;
    private String foodType;
    private BigDecimal densityGramsPerCm3;
    private BigDecimal suggestedWeightG;
    private BigDecimal suggestedVolumeCm3;
    private BigDecimal estimatedKcal;
    private BigDecimal proteinG;
    private BigDecimal carbG;
    private BigDecimal fatG;
    private String emoji;
    private String iconType;
    private String iconBgColor;
    private String iconColor;
    private BigDecimal matchScore;
    private String suitabilityNote;

    public String getFoodCode() { return foodCode; }
    public void setFoodCode(String foodCode) { this.foodCode = foodCode; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    public BigDecimal getDensityGramsPerCm3() { return densityGramsPerCm3; }
    public void setDensityGramsPerCm3(BigDecimal densityGramsPerCm3) { this.densityGramsPerCm3 = densityGramsPerCm3; }
    public BigDecimal getSuggestedWeightG() { return suggestedWeightG; }
    public void setSuggestedWeightG(BigDecimal suggestedWeightG) { this.suggestedWeightG = suggestedWeightG; }
    public BigDecimal getSuggestedVolumeCm3() { return suggestedVolumeCm3; }
    public void setSuggestedVolumeCm3(BigDecimal suggestedVolumeCm3) { this.suggestedVolumeCm3 = suggestedVolumeCm3; }
    public BigDecimal getEstimatedKcal() { return estimatedKcal; }
    public void setEstimatedKcal(BigDecimal estimatedKcal) { this.estimatedKcal = estimatedKcal; }
    public BigDecimal getProteinG() { return proteinG; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }
    public BigDecimal getCarbG() { return carbG; }
    public void setCarbG(BigDecimal carbG) { this.carbG = carbG; }
    public BigDecimal getFatG() { return fatG; }
    public void setFatG(BigDecimal fatG) { this.fatG = fatG; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }
    public String getIconBgColor() { return iconBgColor; }
    public void setIconBgColor(String iconBgColor) { this.iconBgColor = iconBgColor; }
    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }
    public BigDecimal getMatchScore() { return matchScore; }
    public void setMatchScore(BigDecimal matchScore) { this.matchScore = matchScore; }
    public String getSuitabilityNote() { return suitabilityNote; }
    public void setSuitabilityNote(String suitabilityNote) { this.suitabilityNote = suitabilityNote; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String foodCode;
        private String displayName;
        private String category;
        private String foodType;
        private BigDecimal densityGramsPerCm3;
        private BigDecimal suggestedWeightG;
        private BigDecimal suggestedVolumeCm3;
        private BigDecimal estimatedKcal;
        private BigDecimal proteinG;
        private BigDecimal carbG;
        private BigDecimal fatG;
        private String emoji;
        private String iconType;
        private String iconBgColor;
        private String iconColor;
        private BigDecimal matchScore;
        private String suitabilityNote;

        public Builder foodCode(String foodCode) { this.foodCode = foodCode; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder foodType(String foodType) { this.foodType = foodType; return this; }
        public Builder densityGramsPerCm3(BigDecimal densityGramsPerCm3) { this.densityGramsPerCm3 = densityGramsPerCm3; return this; }
        public Builder suggestedWeightG(BigDecimal suggestedWeightG) { this.suggestedWeightG = suggestedWeightG; return this; }
        public Builder suggestedVolumeCm3(BigDecimal suggestedVolumeCm3) { this.suggestedVolumeCm3 = suggestedVolumeCm3; return this; }
        public Builder estimatedKcal(BigDecimal estimatedKcal) { this.estimatedKcal = estimatedKcal; return this; }
        public Builder proteinG(BigDecimal proteinG) { this.proteinG = proteinG; return this; }
        public Builder carbG(BigDecimal carbG) { this.carbG = carbG; return this; }
        public Builder fatG(BigDecimal fatG) { this.fatG = fatG; return this; }
        public Builder emoji(String emoji) { this.emoji = emoji; return this; }
        public Builder iconType(String iconType) { this.iconType = iconType; return this; }
        public Builder iconBgColor(String iconBgColor) { this.iconBgColor = iconBgColor; return this; }
        public Builder iconColor(String iconColor) { this.iconColor = iconColor; return this; }
        public Builder matchScore(BigDecimal matchScore) { this.matchScore = matchScore; return this; }
        public Builder suitabilityNote(String suitabilityNote) { this.suitabilityNote = suitabilityNote; return this; }

        public RecommendedDishItemDTO build() {
            RecommendedDishItemDTO dto = new RecommendedDishItemDTO();
            dto.foodCode = this.foodCode;
            dto.displayName = this.displayName;
            dto.category = this.category;
            dto.foodType = this.foodType;
            dto.densityGramsPerCm3 = this.densityGramsPerCm3;
            dto.suggestedWeightG = this.suggestedWeightG;
            dto.suggestedVolumeCm3 = this.suggestedVolumeCm3;
            dto.estimatedKcal = this.estimatedKcal;
            dto.proteinG = this.proteinG;
            dto.carbG = this.carbG;
            dto.fatG = this.fatG;
            dto.emoji = this.emoji;
            dto.iconType = this.iconType;
            dto.iconBgColor = this.iconBgColor;
            dto.iconColor = this.iconColor;
            dto.matchScore = this.matchScore;
            dto.suitabilityNote = this.suitabilityNote;
            return dto;
        }
    }
}
