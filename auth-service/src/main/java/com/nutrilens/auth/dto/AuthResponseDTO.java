package com.nutrilens.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    @Builder.Default
    private Boolean success = true;
    @Builder.Default
    private String message = "Success";
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private Integer dailyCalorieGoal;
    private String gender;
    private Integer age;
    private String activityLevel;
    private String fitnessGoal;
}
