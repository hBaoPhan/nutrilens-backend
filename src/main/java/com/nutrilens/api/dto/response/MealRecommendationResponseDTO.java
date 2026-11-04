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
public class MealRecommendationResponseDTO {
    private Long userId;
    private String username;
    private String targetMealType;
    private String fitnessGoal;
    private Integer dailyCalorieGoal;
    private BigDecimal todayConsumedKcal;
    private BigDecimal remainingKcalToday;
    private BigDecimal todayConsumedProteinG;
    private BigDecimal todayConsumedCarbG;
    private BigDecimal todayConsumedFatG;
    private BigDecimal targetMealKcal;
    private BigDecimal targetMealProteinG;
    private String aiReasoningSummary;
    private String recommendationSource;
    private List<RecommendedDishItemDTO> recommendedDishes;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTargetMealType() { return targetMealType; }
    public void setTargetMealType(String targetMealType) { this.targetMealType = targetMealType; }
    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }
    public Integer getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(Integer dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }
    public BigDecimal getTodayConsumedKcal() { return todayConsumedKcal; }
    public void setTodayConsumedKcal(BigDecimal todayConsumedKcal) { this.todayConsumedKcal = todayConsumedKcal; }
    public BigDecimal getRemainingKcalToday() { return remainingKcalToday; }
    public void setRemainingKcalToday(BigDecimal remainingKcalToday) { this.remainingKcalToday = remainingKcalToday; }
    public BigDecimal getTodayConsumedProteinG() { return todayConsumedProteinG; }
    public void setTodayConsumedProteinG(BigDecimal todayConsumedProteinG) { this.todayConsumedProteinG = todayConsumedProteinG; }
    public BigDecimal getTodayConsumedCarbG() { return todayConsumedCarbG; }
    public void setTodayConsumedCarbG(BigDecimal todayConsumedCarbG) { this.todayConsumedCarbG = todayConsumedCarbG; }
    public BigDecimal getTodayConsumedFatG() { return todayConsumedFatG; }
    public void setTodayConsumedFatG(BigDecimal todayConsumedFatG) { this.todayConsumedFatG = todayConsumedFatG; }
    public BigDecimal getTargetMealKcal() { return targetMealKcal; }
    public void setTargetMealKcal(BigDecimal targetMealKcal) { this.targetMealKcal = targetMealKcal; }
    public BigDecimal getTargetMealProteinG() { return targetMealProteinG; }
    public void setTargetMealProteinG(BigDecimal targetMealProteinG) { this.targetMealProteinG = targetMealProteinG; }
    public String getAiReasoningSummary() { return aiReasoningSummary; }
    public void setAiReasoningSummary(String aiReasoningSummary) { this.aiReasoningSummary = aiReasoningSummary; }
    public String getRecommendationSource() { return recommendationSource; }
    public void setRecommendationSource(String recommendationSource) { this.recommendationSource = recommendationSource; }
    public List<RecommendedDishItemDTO> getRecommendedDishes() { return recommendedDishes; }
    public void setRecommendedDishes(List<RecommendedDishItemDTO> recommendedDishes) { this.recommendedDishes = recommendedDishes; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long userId;
        private String username;
        private String targetMealType;
        private String fitnessGoal;
        private Integer dailyCalorieGoal;
        private BigDecimal todayConsumedKcal;
        private BigDecimal remainingKcalToday;
        private BigDecimal todayConsumedProteinG;
        private BigDecimal todayConsumedCarbG;
        private BigDecimal todayConsumedFatG;
        private BigDecimal targetMealKcal;
        private BigDecimal targetMealProteinG;
        private String aiReasoningSummary;
        private String recommendationSource;
        private List<RecommendedDishItemDTO> recommendedDishes;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder targetMealType(String targetMealType) { this.targetMealType = targetMealType; return this; }
        public Builder fitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; return this; }
        public Builder dailyCalorieGoal(Integer dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; return this; }
        public Builder todayConsumedKcal(BigDecimal todayConsumedKcal) { this.todayConsumedKcal = todayConsumedKcal; return this; }
        public Builder remainingKcalToday(BigDecimal remainingKcalToday) { this.remainingKcalToday = remainingKcalToday; return this; }
        public Builder todayConsumedProteinG(BigDecimal todayConsumedProteinG) { this.todayConsumedProteinG = todayConsumedProteinG; return this; }
        public Builder todayConsumedCarbG(BigDecimal todayConsumedCarbG) { this.todayConsumedCarbG = todayConsumedCarbG; return this; }
        public Builder todayConsumedFatG(BigDecimal todayConsumedFatG) { this.todayConsumedFatG = todayConsumedFatG; return this; }
        public Builder targetMealKcal(BigDecimal targetMealKcal) { this.targetMealKcal = targetMealKcal; return this; }
        public Builder targetMealProteinG(BigDecimal targetMealProteinG) { this.targetMealProteinG = targetMealProteinG; return this; }
        public Builder aiReasoningSummary(String aiReasoningSummary) { this.aiReasoningSummary = aiReasoningSummary; return this; }
        public Builder recommendationSource(String recommendationSource) { this.recommendationSource = recommendationSource; return this; }
        public Builder recommendedDishes(List<RecommendedDishItemDTO> recommendedDishes) { this.recommendedDishes = recommendedDishes; return this; }

        public MealRecommendationResponseDTO build() {
            MealRecommendationResponseDTO dto = new MealRecommendationResponseDTO();
            dto.userId = this.userId;
            dto.username = this.username;
            dto.targetMealType = this.targetMealType;
            dto.fitnessGoal = this.fitnessGoal;
            dto.dailyCalorieGoal = this.dailyCalorieGoal;
            dto.todayConsumedKcal = this.todayConsumedKcal;
            dto.remainingKcalToday = this.remainingKcalToday;
            dto.todayConsumedProteinG = this.todayConsumedProteinG;
            dto.todayConsumedCarbG = this.todayConsumedCarbG;
            dto.todayConsumedFatG = this.todayConsumedFatG;
            dto.targetMealKcal = this.targetMealKcal;
            dto.targetMealProteinG = this.targetMealProteinG;
            dto.aiReasoningSummary = this.aiReasoningSummary;
            dto.recommendationSource = this.recommendationSource;
            dto.recommendedDishes = this.recommendedDishes;
            return dto;
        }
    }
}
