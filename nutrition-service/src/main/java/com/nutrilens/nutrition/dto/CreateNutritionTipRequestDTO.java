package com.nutrilens.nutrition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNutritionTipRequestDTO {

    @NotBlank(message = "Tiêu đề mẹo dinh dưỡng không được để trống")
    @Size(max = 150, message = "Tiêu đề không được vượt quá 150 ký tự")
    private String title;

    @NotBlank(message = "Nội dung mẹo dinh dưỡng không được để trống")
    @Size(max = 500, message = "Nội dung không được vượt quá 500 ký tự")
    private String content;

    @NotBlank(message = "Danh mục mẹo không được để trống")
    @Size(max = 50, message = "Danh mục không được vượt quá 50 ký tự")
    private String category;

    @Builder.Default
    private String iconType = "salad";

    @Builder.Default
    private String targetGoal = "ALL";

    @Builder.Default
    private Boolean isActive = true;
}
