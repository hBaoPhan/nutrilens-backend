package com.nutrilens.api.repository;

import com.nutrilens.api.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    List<MealLog> findAllByOrderByLoggedAtDesc();
    List<MealLog> findByUserIdOrderByLoggedAtDesc(Long userId);
    List<MealLog> findByUserUsernameOrderByLoggedAtDesc(String username);

    List<MealLog> findByUserIdAndLoggedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    List<MealLog> findByUserUsernameAndLoggedAtBetween(String username, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT m FROM MealLog m WHERE " +
           "(:username IS NULL OR LOWER(m.user.username) = LOWER(:username)) AND " +
           "(:startDate IS NULL OR m.loggedAt >= :startDate) AND " +
           "(:endDate IS NULL OR m.loggedAt < :endDate) AND " +
           "(:mealType IS NULL OR UPPER(m.mealType) = UPPER(:mealType)) " +
           "ORDER BY m.loggedAt DESC")
    List<MealLog> findFiltered(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("mealType") String mealType);
}
