package com.nutrilens.nutrition.repository;

import com.nutrilens.nutrition.entity.NutritionTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionTipRepository extends JpaRepository<NutritionTip, Long> {
    List<NutritionTip> findAllByIsActiveTrue();
    List<NutritionTip> findAllByTargetGoalIgnoreCaseAndIsActiveTrue(String targetGoal);
    List<NutritionTip> findAllByCategoryIgnoreCaseAndIsActiveTrue(String category);
}
