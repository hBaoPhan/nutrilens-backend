package com.nutrilens.recommendation.service.impl;

import com.nutrilens.common.dto.response.FoodItemDTO;
import com.nutrilens.common.dto.response.UserSummaryDTO;
import com.nutrilens.recommendation.client.AuthServiceClient;
import com.nutrilens.recommendation.client.MealServiceClient;
import com.nutrilens.recommendation.client.NutritionServiceClient;
import com.nutrilens.recommendation.dto.MealRecommendationResponseDTO;
import com.nutrilens.recommendation.service.MealRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealRecommendationServiceImpl implements MealRecommendationService {

    private final AuthServiceClient authServiceClient;
    private final MealServiceClient mealServiceClient;
    private final NutritionServiceClient nutritionServiceClient;

    @Override
    public MealRecommendationResponseDTO getNextMealRecommendation(String username) {
        String cleanUser = (username != null && !username.isBlank()) ? username.trim() : "baophan";
        log.info("[RECOMMENDATION SERVICE] Generating next meal recommendation for: {}", cleanUser);

        // 1. Fetch user profile
        UserSummaryDTO user = authServiceClient.getUserSummary(cleanUser);
        BigDecimal targetKcal = (user != null && user.getDailyCalorieGoal() != null)
                ? BigDecimal.valueOf(user.getDailyCalorieGoal())
                : BigDecimal.valueOf(2000.0);
        String goal = (user != null && user.getFitnessGoal() != null)
                ? user.getFitnessGoal()
                : "MAINTAIN";

        // 2. Fetch consumed calories today
        BigDecimal consumedKcal = mealServiceClient.getTodayCalories(cleanUser);
        if (consumedKcal == null) consumedKcal = BigDecimal.ZERO;

        BigDecimal remainingKcal = targetKcal.subtract(consumedKcal);

        // 3. Determine meal time slot
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String mealType;
        String searchCategory;
        if (now.isBefore(LocalTime.of(10, 30)) && now.isAfter(LocalTime.of(4, 59))) {
            mealType = "Bữa Sáng (Breakfast)";
            searchCategory = "Breakfast";
        } else if (now.isBefore(LocalTime.of(15, 0))) {
            mealType = "Bữa Trưa (Lunch)";
            searchCategory = "Lunch";
        } else if (now.isBefore(LocalTime.of(17, 30))) {
            mealType = "Bữa Xế (Afternoon Snack)";
            searchCategory = "Snack";
        } else if (now.isBefore(LocalTime.of(22, 0))) {
            mealType = "Bữa Tối (Dinner)";
            searchCategory = "Dinner";
        } else {
            mealType = "Bữa Nhẹ Đêm (Late Night)";
            searchCategory = "Snack";
        }

        // 4. Fetch dishes from Nutrition Service
        List<FoodItemDTO> dishes = nutritionServiceClient.getFoods(null);
        if (dishes == null || dishes.isEmpty()) {
            dishes = nutritionServiceClient.getFoods(searchCategory);
        }

        // 5. Select recommended dish based on goal and remaining calories
        String status;
        if (remainingKcal.compareTo(BigDecimal.valueOf(200)) < 0) {
            status = "SURPLUS";
        } else if (remainingKcal.compareTo(BigDecimal.valueOf(800)) > 0) {
            status = "DEFICIT";
        } else {
            status = "ON_TRACK";
        }

        FoodItemDTO chosenDish = null;
        if (dishes != null && !dishes.isEmpty()) {
            if ("LOSE_WEIGHT".equalsIgnoreCase(goal) || "SURPLUS".equals(status)) {
                // Recommend lowest kcal per 100g
                chosenDish = dishes.stream()
                        .min((a, b) -> Double.compare(
                                a.getKcalPer100g() != null ? a.getKcalPer100g() : 0.0,
                                b.getKcalPer100g() != null ? b.getKcalPer100g() : 0.0))
                        .orElse(dishes.get(0));
            } else if ("GAIN_MUSCLE".equalsIgnoreCase(goal)) {
                // Recommend highest protein
                chosenDish = dishes.stream()
                        .max((a, b) -> Double.compare(
                                a.getProteinPer100g() != null ? a.getProteinPer100g() : 0.0,
                                b.getProteinPer100g() != null ? b.getProteinPer100g() : 0.0))
                        .orElse(dishes.get(0));
            } else {
                chosenDish = dishes.get(0);
            }
        }

        String dishName = (chosenDish != null) ? chosenDish.getDisplayName() : "";
        String foodCode = (chosenDish != null) ? chosenDish.getFoodCode() : "";

        // 6. Generate AI Nutrition Guidance
        StringBuilder advice = new StringBuilder();
        if (chosenDish == null) {
            advice.append("Hiện chưa có món ăn nào trong cơ sở dữ liệu để đưa ra gợi ý.");
        } else {
            advice.append("Bạn còn lại khoảng ").append(remainingKcal.setScale(0, RoundingMode.HALF_UP)).append(" kcal cho ngày hôm nay. ");

            if ("SURPLUS".equals(status)) {
                advice.append("Lượng calo đã gần chạm ngưỡng mục tiêu TDEE. Cho ").append(mealType)
                      .append(", hãy ưu tiên các món thanh đạm, giàu rau xanh và nước dùng trong như ")
                      .append(dishName).append(" để giữ mức cân bằng lý tưởng.");
            } else if ("DEFICIT".equals(status)) {
                advice.append("Bạn còn thâm hụt calo khá nhiều. Hãy nạp đủ năng lượng với món ")
                      .append(dishName).append(" giàu dinh dưỡng, kết hợp protein để tối ưu hóa quá trình trao đổi chất.");
            } else {
                advice.append("Bạn đang duy trì tiến độ calo rất xuất sắc! Món ")
                      .append(dishName).append(" là sự lựa chọn hài hòa cả về đạm, tinh bột và vi chất cho ")
                      .append(mealType).append(".");
            }
        }

        List<FoodItemDTO> candidates = (dishes != null)
                ? dishes.stream().limit(5).collect(Collectors.toList())
                : List.of();

        return MealRecommendationResponseDTO.builder()
                .username(cleanUser)
                .currentMealType(mealType)
                .recommendedDish(dishName)
                .foodCode(foodCode)
                .targetDailyKcal(targetKcal)
                .consumedTodayKcal(consumedKcal)
                .remainingTodayKcal(remainingKcal)
                .status(status)
                .healthGoal(goal)
                .aiAdvice(advice.toString())
                .candidateDishes(candidates)
                .build();
    }
}
