package com.nutrilens.auth.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDTO {

    @NotNull(message = "Cân nặng không được để trống")
    @DecimalMin(value = "30.00", message = "Cân nặng tối thiểu là 30kg")
    @DecimalMax(value = "300.00", message = "Cân nặng tối đa là 300kg")
    private BigDecimal weightKg;

    @NotNull(message = "Chiều cao không được để trống")
    @DecimalMin(value = "80.00", message = "Chiều cao tối thiểu là 80cm")
    @DecimalMax(value = "250.00", message = "Chiều cao tối đa là 250cm")
    private BigDecimal heightCm;

    @NotNull(message = "Mục tiêu calo không được để trống")
    @Min(value = 800, message = "Mục tiêu calo tối thiểu là 800 kcal")
    @Max(value = 6000, message = "Mục tiêu calo tối đa là 6000 kcal")
    private Integer dailyCalorieGoal;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Giới tính phải là MALE, FEMALE hoặc OTHER")
    private String gender;

    @Min(value = 10, message = "Tuổi tối thiểu là 10")
    @Max(value = 120, message = "Tuổi tối đa là 120")
    private Integer age;

    @Size(max = 30)
    private String activityLevel;

    @Size(max = 30)
    private String fitnessGoal;
}
