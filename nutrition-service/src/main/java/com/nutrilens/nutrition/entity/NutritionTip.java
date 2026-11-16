package com.nutrilens.nutrition.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_tips", indexes = {
    @Index(name = "idx_tips_active", columnList = "is_active"),
    @Index(name = "idx_tips_target", columnList = "target_goal")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionTip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "icon_type", nullable = false, length = 50)
    private String iconType;

    @Column(name = "target_goal", nullable = false, length = 50)
    private String targetGoal;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
