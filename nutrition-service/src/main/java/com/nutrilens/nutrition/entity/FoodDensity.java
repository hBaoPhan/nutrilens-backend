package com.nutrilens.nutrition.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_densities", indexes = {
    @Index(name = "idx_food_code", columnList = "food_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodDensity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_code", nullable = false, unique = true, length = 50)
    private String foodCode;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "category", length = 50)
    private String category = "Breakfast";

    @Column(name = "food_type", length = 30)
    private String foodType = "COMPOSITE_DISH";

    @Column(name = "density_g_per_cm3", nullable = false, precision = 6, scale = 4)
    private BigDecimal densityGramsPerCm3;

    @Column(name = "kcal_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal kcalPer100g;

    @Column(name = "protein_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal proteinPer100g;

    @Column(name = "carb_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal carbPer100g;

    @Column(name = "fat_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal fatPer100g;

    @Column(name = "serving_weight_g", precision = 6, scale = 2)
    private BigDecimal servingWeightG = new BigDecimal("100.00");

    @Column(name = "serving_volume_cm3", precision = 6, scale = 2)
    private BigDecimal servingVolumeCm3 = new BigDecimal("150.00");

    @Column(name = "emoji", length = 20)
    private String emoji;

    @Column(name = "icon_type", length = 50)
    private String iconType = "salad";

    @Column(name = "icon_bg_color", length = 20)
    private String iconBgColor = "#FFEFE6";

    @Column(name = "icon_color", length = 20)
    private String iconColor = "#F37340";

    @Column(name = "is_highlighted_more")
    private Boolean isHighlightedMore = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
