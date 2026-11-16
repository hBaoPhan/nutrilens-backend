package com.nutrilens.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Double weightKg;
    private Double heightCm;
    private String gender;
    private Integer dailyCalorieGoal;
    private Integer age;
    private String activityLevel;
    private String fitnessGoal;
    private String role;
}
