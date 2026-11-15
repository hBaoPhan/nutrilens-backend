package com.nutrilens.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateFoodItemRequestDTO {

    @NotBlank(message = "Mã món ăn không được để trống")
    @Size(max = 50, message = "Mã món ăn tối đa 50 ký tự")
    private String foodCode;

    @NotBlank(message = "Tên hiển thị món ăn không được để trống")
    @Size(max = 150, message = "Tên hiển thị tối đa 150 ký tự")
    private String displayName;

    @NotBlank(message = "Danh mục không được để trống")
    @Size(max = 50, message = "Danh mục tối đa 50 ký tự")
    private String category;

    private String foodType;

    @NotNull(message = "Mật độ khối lượng riêng rho (g/cm3) không được để trống")
    @DecimalMin(value = "0.01", message = "Mật độ khối lượng riêng phải lớn hơn 0")
    private BigDecimal densityGramsPerCm3;

    @NotNull(message = "Calo trên 100g không được để trống")
    @DecimalMin(value = "0.0", message = "Calo không được âm")
    private BigDecimal kcalPer100g;

    @NotNull(message = "Protein trên 100g không được để trống")
    @DecimalMin(value = "0.0", message = "Protein không được âm")
    private BigDecimal proteinPer100g;

    @NotNull(message = "Carb trên 100g không được để trống")
    @DecimalMin(value = "0.0", message = "Carb không được âm")
    private BigDecimal carbPer100g;

    @NotNull(message = "Chất béo trên 100g không được để trống")
    @DecimalMin(value = "0.0", message = "Chất béo không được âm")
    private BigDecimal fatPer100g;

    private BigDecimal servingWeightG;
    private BigDecimal servingVolumeCm3;
    private String emoji;
    private String iconType;
    private String iconBgColor;
    private String iconColor;

    public CreateFoodItemRequestDTO() {}

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

    public BigDecimal getKcalPer100g() { return kcalPer100g; }
    public void setKcalPer100g(BigDecimal kcalPer100g) { this.kcalPer100g = kcalPer100g; }

    public BigDecimal getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; }

    public BigDecimal getCarbPer100g() { return carbPer100g; }
    public void setCarbPer100g(BigDecimal carbPer100g) { this.carbPer100g = carbPer100g; }

    public BigDecimal getFatPer100g() { return fatPer100g; }
    public void setFatPer100g(BigDecimal fatPer100g) { this.fatPer100g = fatPer100g; }

    public BigDecimal getServingWeightG() { return servingWeightG; }
    public void setServingWeightG(BigDecimal servingWeightG) { this.servingWeightG = servingWeightG; }

    public BigDecimal getServingVolumeCm3() { return servingVolumeCm3; }
    public void setServingVolumeCm3(BigDecimal servingVolumeCm3) { this.servingVolumeCm3 = servingVolumeCm3; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }

    public String getIconBgColor() { return iconBgColor; }
    public void setIconBgColor(String iconBgColor) { this.iconBgColor = iconBgColor; }

    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }
}
