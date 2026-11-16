package com.nutrilens.meal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_items", indexes = {
    @Index(name = "idx_item_meal_log", columnList = "meal_log_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_log_id", nullable = false)
    @JsonIgnoreProperties("items")
    private MealLog mealLog;

    @Column(name = "food_density_id", nullable = false)
    private Long foodDensityId;

    @Column(name = "food_code", nullable = false, length = 50)
    private String foodCode;

    @Column(name = "food_name", nullable = false, length = 150)
    private String foodName;

    @Column(name = "raw_volume_cm3", nullable = false, precision = 8, scale = 2)
    private BigDecimal rawVolumeCm3;

    @Column(name = "portion_ratio", nullable = false, precision = 4, scale = 3)
    @Builder.Default
    private BigDecimal portionRatio = BigDecimal.ONE;

    @Column(name = "effective_volume_cm3", nullable = false, precision = 8, scale = 2)
    private BigDecimal effectiveVolumeCm3;

    @Column(name = "estimated_weight_g", nullable = false, precision = 8, scale = 2)
    private BigDecimal estimatedWeightG;

    @Column(name = "calculated_kcal", nullable = false, precision = 8, scale = 2)
    private BigDecimal calculatedKcal;

    @Column(name = "protein_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "carb_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal carbG;

    @Column(name = "fat_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal fatG;

    @Column(name = "confidence_score", precision = 4, scale = 3)
    @Builder.Default
    private BigDecimal confidenceScore = new BigDecimal("0.950");

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
