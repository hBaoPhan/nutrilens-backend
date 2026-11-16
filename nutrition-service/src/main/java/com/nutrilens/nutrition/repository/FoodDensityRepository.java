package com.nutrilens.nutrition.repository;

import com.nutrilens.nutrition.entity.FoodDensity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodDensityRepository extends JpaRepository<FoodDensity, Long> {
    Optional<FoodDensity> findByFoodCode(String foodCode);
    Optional<FoodDensity> findByFoodCodeAndIsActiveTrue(String foodCode);
    List<FoodDensity> findAllByIsActiveTrue();
    List<FoodDensity> findAllByCategoryIgnoreCaseAndIsActiveTrue(String category);
    boolean existsByFoodCode(String foodCode);
}
