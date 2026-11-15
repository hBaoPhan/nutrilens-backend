package com.nutrilens.api.dto.response;

import java.math.BigDecimal;

public class AdminSystemStatsResponseDTO {

    private long totalFoods;
    private long totalMealLogs;
    private long totalUsers;
    private long totalTips;
    private BigDecimal averageKcalPerMeal;
    private String springBootStatus;
    private String aiServiceStatus;
    private String databaseStatus;

    public AdminSystemStatsResponseDTO() {}

    public AdminSystemStatsResponseDTO(long totalFoods, long totalMealLogs, long totalUsers, long totalTips,
                                      BigDecimal averageKcalPerMeal, String springBootStatus,
                                      String aiServiceStatus, String databaseStatus) {
        this.totalFoods = totalFoods;
        this.totalMealLogs = totalMealLogs;
        this.totalUsers = totalUsers;
        this.totalTips = totalTips;
        this.averageKcalPerMeal = averageKcalPerMeal;
        this.springBootStatus = springBootStatus;
        this.aiServiceStatus = aiServiceStatus;
        this.databaseStatus = databaseStatus;
    }

    public long getTotalFoods() { return totalFoods; }
    public void setTotalFoods(long totalFoods) { this.totalFoods = totalFoods; }

    public long getTotalMealLogs() { return totalMealLogs; }
    public void setTotalMealLogs(long totalMealLogs) { this.totalMealLogs = totalMealLogs; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalTips() { return totalTips; }
    public void setTotalTips(long totalTips) { this.totalTips = totalTips; }

    public BigDecimal getAverageKcalPerMeal() { return averageKcalPerMeal; }
    public void setAverageKcalPerMeal(BigDecimal averageKcalPerMeal) { this.averageKcalPerMeal = averageKcalPerMeal; }

    public String getSpringBootStatus() { return springBootStatus; }
    public void setSpringBootStatus(String springBootStatus) { this.springBootStatus = springBootStatus; }

    public String getAiServiceStatus() { return aiServiceStatus; }
    public void setAiServiceStatus(String aiServiceStatus) { this.aiServiceStatus = aiServiceStatus; }

    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalFoods;
        private long totalMealLogs;
        private long totalUsers;
        private long totalTips;
        private BigDecimal averageKcalPerMeal;
        private String springBootStatus;
        private String aiServiceStatus;
        private String databaseStatus;

        public Builder totalFoods(long totalFoods) { this.totalFoods = totalFoods; return this; }
        public Builder totalMealLogs(long totalMealLogs) { this.totalMealLogs = totalMealLogs; return this; }
        public Builder totalUsers(long totalUsers) { this.totalUsers = totalUsers; return this; }
        public Builder totalTips(long totalTips) { this.totalTips = totalTips; return this; }
        public Builder averageKcalPerMeal(BigDecimal averageKcalPerMeal) { this.averageKcalPerMeal = averageKcalPerMeal; return this; }
        public Builder springBootStatus(String springBootStatus) { this.springBootStatus = springBootStatus; return this; }
        public Builder aiServiceStatus(String aiServiceStatus) { this.aiServiceStatus = aiServiceStatus; return this; }
        public Builder databaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; return this; }

        public AdminSystemStatsResponseDTO build() {
            return new AdminSystemStatsResponseDTO(totalFoods, totalMealLogs, totalUsers, totalTips,
                    averageKcalPerMeal, springBootStatus, aiServiceStatus, databaseStatus);
        }
    }
}
