package com.nutrilens.meal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_logs", indexes = {
    @Index(name = "idx_meal_user_logged", columnList = "user_id, logged_at"),
    @Index(name = "idx_meal_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @Column(name = "total_volume_cm3", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal totalVolumeCm3 = BigDecimal.ZERO;

    @Column(name = "total_weight_g", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal totalWeightG = BigDecimal.ZERO;

    @Column(name = "total_kcal", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal totalKcal = BigDecimal.ZERO;

    @Column(name = "total_protein_g", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal totalProteinG = BigDecimal.ZERO;

    @Column(name = "total_carb_g", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal totalCarbG = BigDecimal.ZERO;

    @Column(name = "total_fat_g", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal totalFatG = BigDecimal.ZERO;

    @Column(name = "ar_snapshot_url", length = 500)
    private String arSnapshotUrl;

    @Column(name = "user_notes", length = 2000)
    private String userNotes;

    @OneToMany(mappedBy = "mealLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("mealLog")
    @Builder.Default
    private List<MealItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
