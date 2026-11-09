package com.nutrilens.api.service.impl;

import com.nutrilens.api.dto.response.MealRecommendationResponseDTO;
import com.nutrilens.api.dto.response.RecommendedDishItemDTO;
import com.nutrilens.api.entity.FoodDensity;
import com.nutrilens.api.entity.MealItem;
import com.nutrilens.api.entity.MealLog;
import com.nutrilens.api.entity.User;
import com.nutrilens.api.repository.FoodDensityRepository;
import com.nutrilens.api.repository.MealLogRepository;
import com.nutrilens.api.repository.UserRepository;
import com.nutrilens.api.service.MealRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealRecommendationServiceImpl implements MealRecommendationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MealRecommendationServiceImpl.class);

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final FoodDensityRepository foodDensityRepository;

    @Value("${app.ai-service-url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(5000))
            .setReadTimeout(Duration.ofMillis(12000))
            .build();

    public MealRecommendationServiceImpl(UserRepository userRepository,
                                         MealLogRepository mealLogRepository,
                                         FoodDensityRepository foodDensityRepository) {
        this.userRepository = userRepository;
        this.mealLogRepository = mealLogRepository;
        this.foodDensityRepository = foodDensityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MealRecommendationResponseDTO getNextMealRecommendation(Long userId, String username) {
        log.info("Generating next-meal RAG recommendation for userId={}, username={}", userId, username);

        // 1. Resolve User entity or fallback to active profile
        User user = resolveUser(userId, username);

        // 2. Query today's logged meals
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<MealLog> todayLogs = user.getId() != null
                ? mealLogRepository.findByUserIdAndLoggedAtBetween(user.getId(), startOfDay, endOfDay)
                : Collections.emptyList();

        // 3. Compute consumed nutrients today
        BigDecimal todayConsumedKcal = BigDecimal.ZERO;
        BigDecimal todayConsumedProtein = BigDecimal.ZERO;
        BigDecimal todayConsumedCarb = BigDecimal.ZERO;
        BigDecimal todayConsumedFat = BigDecimal.ZERO;
        Set<String> eatenMealTypes = new HashSet<>();
        Set<String> eatenFoodCodes = new HashSet<>();

        for (MealLog logItem : todayLogs) {
            if (logItem.getMealType() != null) {
                eatenMealTypes.add(logItem.getMealType().toUpperCase());
            }
            if (logItem.getTotalKcal() != null) {
                todayConsumedKcal = todayConsumedKcal.add(logItem.getTotalKcal());
            }
            if (logItem.getTotalProteinG() != null) {
                todayConsumedProtein = todayConsumedProtein.add(logItem.getTotalProteinG());
            }
            if (logItem.getTotalCarbG() != null) {
                todayConsumedCarb = todayConsumedCarb.add(logItem.getTotalCarbG());
            }
            if (logItem.getTotalFatG() != null) {
                todayConsumedFat = todayConsumedFat.add(logItem.getTotalFatG());
            }
            if (logItem.getItems() != null) {
                for (MealItem mi : logItem.getItems()) {
                    if (mi.getFoodDensity() != null && mi.getFoodDensity().getFoodCode() != null) {
                        eatenFoodCodes.add(mi.getFoodDensity().getFoodCode());
                    }
                }
            }
        }

        // 4. Compute remaining daily calorie budget
        BigDecimal dailyGoal = BigDecimal.valueOf(user.getDailyCalorieGoal() != null ? user.getDailyCalorieGoal() : 2000);
        BigDecimal remainingKcal = dailyGoal.subtract(todayConsumedKcal).max(BigDecimal.ZERO);

        // 5. Determine next target meal type and portion budget
        int currentHour = LocalTime.now().getHour();
        String targetMealType;
        BigDecimal mealBudgetRatio;

        if (!eatenMealTypes.contains("BREAKFAST") && currentHour < 11) {
            targetMealType = "BREAKFAST";
            mealBudgetRatio = new BigDecimal("0.25");
        } else if (!eatenMealTypes.contains("LUNCH") && currentHour < 15) {
            targetMealType = "LUNCH";
            mealBudgetRatio = new BigDecimal("0.40");
        } else if (!eatenMealTypes.contains("DINNER") && currentHour < 21) {
            targetMealType = "DINNER";
            mealBudgetRatio = new BigDecimal("0.35");
        } else {
            targetMealType = remainingKcal.compareTo(new BigDecimal("150")) > 0 ? "SNACK" : "DINNER";
            mealBudgetRatio = new BigDecimal("0.20");
        }

        // Target calories for this specific meal
        BigDecimal targetMealKcal = remainingKcal.compareTo(BigDecimal.ZERO) > 0
                ? dailyGoal.multiply(mealBudgetRatio).min(remainingKcal).setScale(0, RoundingMode.HALF_UP)
                : new BigDecimal("150");

        // Target protein based on bodyweight and fitness goal
        BigDecimal userWeight = user.getWeightKg() != null ? user.getWeightKg() : new BigDecimal("65");
        BigDecimal proteinMultiplier = "GAIN_WEIGHT".equalsIgnoreCase(user.getFitnessGoal())
                ? new BigDecimal("1.8")
                : ("LOSE_WEIGHT".equalsIgnoreCase(user.getFitnessGoal()) ? new BigDecimal("1.5") : new BigDecimal("1.2"));
        BigDecimal dailyProteinGoal = userWeight.multiply(proteinMultiplier);
        BigDecimal targetMealProtein = dailyProteinGoal.multiply(mealBudgetRatio).setScale(1, RoundingMode.HALF_UP);

        // 6. Retrieve active candidates from Database
        List<FoodDensity> activeFoods = foodDensityRepository.findAllByIsActiveTrue();

        // 7. Attempt RAG inference via AI Microservice
        MealRecommendationResponseDTO ragResult = callAiMicroserviceRAG(
                user, todayConsumedKcal, remainingKcal, targetMealType, targetMealKcal, targetMealProtein,
                todayConsumedProtein, todayConsumedCarb, todayConsumedFat, eatenFoodCodes, activeFoods
        );

        if (ragResult != null && ragResult.getRecommendedDishes() != null && !ragResult.getRecommendedDishes().isEmpty()) {
            return ragResult;
        }

        // 8. Fallback: Internal Deterministic Clinical Scoring Algorithm
        log.info("AI microservice RAG offline or unavailable. Executing internal clinical recommendation engine.");
        return executeClinicalScoringRecommendation(
                user, todayConsumedKcal, remainingKcal, targetMealType, targetMealKcal, targetMealProtein,
                todayConsumedProtein, todayConsumedCarb, todayConsumedFat, eatenFoodCodes, activeFoods
        );
    }

    private User resolveUser(Long userId, String username) {
        if (username != null && !username.trim().isEmpty()) {
            Optional<User> u = userRepository.findByUsername(username.trim());
            if (u.isPresent()) return u.get();
        }
        if (userId != null) {
            Optional<User> u = userRepository.findById(userId);
            if (u.isPresent()) return u.get();
        }
        return userRepository.findByUsername("user1")
                .or(() -> userRepository.findAll().stream().findFirst())
                .orElseGet(() -> User.builder()
                        .id(2L)
                        .username("user1")
                        .fullName("Nguyễn Văn An")
                        .weightKg(new BigDecimal("65.00"))
                        .heightCm(new BigDecimal("170.00"))
                        .dailyCalorieGoal(2000)
                        .fitnessGoal("MAINTAIN")
                        .build());
    }

    private MealRecommendationResponseDTO callAiMicroserviceRAG(
            User user, BigDecimal todayConsumedKcal, BigDecimal remainingKcal,
            String targetMealType, BigDecimal targetMealKcal, BigDecimal targetMealProtein,
            BigDecimal consumedProtein, BigDecimal consumedCarb, BigDecimal consumedFat,
            Set<String> eatenFoodCodes, List<FoodDensity> activeFoods) {
        try {
            String baseUrl = (aiServiceUrl != null && !aiServiceUrl.isEmpty()) ? aiServiceUrl : "http://localhost:8000";
            String endpoint = baseUrl.replaceAll("/+$", "") + "/api/v1/recommendations/rag-suggest";

            Map<String, Object> userContext = new HashMap<>();
            userContext.put("userId", user.getId());
            userContext.put("username", user.getUsername());
            userContext.put("fitnessGoal", user.getFitnessGoal() != null ? user.getFitnessGoal() : "MAINTAIN");
            userContext.put("weightKg", user.getWeightKg() != null ? user.getWeightKg().doubleValue() : 65.0);
            userContext.put("heightCm", user.getHeightCm() != null ? user.getHeightCm().doubleValue() : 170.0);
            userContext.put("dailyCalorieGoal", user.getDailyCalorieGoal() != null ? user.getDailyCalorieGoal() : 2000);
            userContext.put("todayConsumedKcal", todayConsumedKcal.doubleValue());
            userContext.put("remainingKcalToday", remainingKcal.doubleValue());
            userContext.put("targetMealType", targetMealType);
            userContext.put("targetMealKcal", targetMealKcal.doubleValue());
            userContext.put("targetMealProteinG", targetMealProtein.doubleValue());
            userContext.put("alreadyEatenFoods", new ArrayList<>(eatenFoodCodes));

            List<Map<String, Object>> candidateFoods = activeFoods.stream().map(f -> {
                Map<String, Object> map = new HashMap<>();
                map.put("foodCode", f.getFoodCode());
                map.put("displayName", f.getDisplayName());
                map.put("category", f.getCategory());
                map.put("foodType", f.getFoodType());
                map.put("densityGramsPerCm3", f.getDensityGramsPerCm3() != null ? f.getDensityGramsPerCm3().doubleValue() : 1.0);
                map.put("servingWeightG", f.getServingWeightG() != null ? f.getServingWeightG().doubleValue() : 100.0);
                map.put("kcalPer100g", f.getKcalPer100g() != null ? f.getKcalPer100g().doubleValue() : 100.0);
                map.put("proteinPer100g", f.getProteinPer100g() != null ? f.getProteinPer100g().doubleValue() : 5.0);
                map.put("carbPer100g", f.getCarbPer100g() != null ? f.getCarbPer100g().doubleValue() : 10.0);
                map.put("fatPer100g", f.getFatPer100g() != null ? f.getFatPer100g().doubleValue() : 2.0);
                map.put("emoji", f.getEmoji());
                map.put("iconType", f.getIconType());
                map.put("iconBgColor", f.getIconBgColor());
                map.put("iconColor", f.getIconColor());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("userContext", userContext);
            requestPayload.put("candidateFoods", candidateFoods);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                String rationale = (String) body.get("aiReasoningSummary");
                String modelSource = (String) body.getOrDefault("modelSource", "AI_RAG_MICROSERVICE");
                List<Map<String, Object>> dishMaps = (List<Map<String, Object>>) body.get("recommendedDishes");

                List<RecommendedDishItemDTO> dishes = new ArrayList<>();
                if (dishMaps != null) {
                    for (Map<String, Object> dm : dishMaps) {
                        dishes.add(RecommendedDishItemDTO.builder()
                                .foodCode((String) dm.get("foodCode"))
                                .displayName((String) dm.get("displayName"))
                                .category((String) dm.get("category"))
                                .foodType((String) dm.get("foodType"))
                                .densityGramsPerCm3(toBigDecimal(dm.get("densityGramsPerCm3")))
                                .suggestedWeightG(toBigDecimal(dm.get("suggestedWeightG")))
                                .suggestedVolumeCm3(toBigDecimal(dm.get("suggestedVolumeCm3")))
                                .estimatedKcal(toBigDecimal(dm.get("estimatedKcal")))
                                .proteinG(toBigDecimal(dm.get("proteinG")))
                                .carbG(toBigDecimal(dm.get("carbG")))
                                .fatG(toBigDecimal(dm.get("fatG")))
                                .emoji((String) dm.get("emoji"))
                                .iconType((String) dm.get("iconType"))
                                .iconBgColor((String) dm.get("iconBgColor"))
                                .iconColor((String) dm.get("iconColor"))
                                .matchScore(toBigDecimal(dm.get("matchScore")))
                                .suitabilityNote((String) dm.get("suitabilityNote"))
                                .build());
                    }
                }

                return MealRecommendationResponseDTO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .targetMealType(targetMealType)
                        .fitnessGoal(user.getFitnessGoal())
                        .dailyCalorieGoal(user.getDailyCalorieGoal())
                        .todayConsumedKcal(todayConsumedKcal)
                        .remainingKcalToday(remainingKcal)
                        .todayConsumedProteinG(consumedProtein)
                        .todayConsumedCarbG(consumedCarb)
                        .todayConsumedFatG(consumedFat)
                        .targetMealKcal(targetMealKcal)
                        .targetMealProteinG(targetMealProtein)
                        .aiReasoningSummary(rationale)
                        .recommendationSource(modelSource)
                        .recommendedDishes(dishes)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed calling AI service RAG endpoint ({}/api/v1/recommendations/rag-suggest): {}", aiServiceUrl, e.getMessage());
        }
        return null;
    }

    private MealRecommendationResponseDTO executeClinicalScoringRecommendation(
            User user, BigDecimal todayConsumedKcal, BigDecimal remainingKcal,
            String targetMealType, BigDecimal targetMealKcal, BigDecimal targetMealProtein,
            BigDecimal consumedProtein, BigDecimal consumedCarb, BigDecimal consumedFat,
            Set<String> eatenFoodCodes, List<FoodDensity> activeFoods) {

        List<ScoredDish> scoredList = new ArrayList<>();

        for (FoodDensity food : activeFoods) {
            BigDecimal servingWeight = food.getServingWeightG() != null ? food.getServingWeightG() : new BigDecimal("100.00");
            BigDecimal factor = servingWeight.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);

            BigDecimal dishKcal = food.getKcalPer100g().multiply(factor).setScale(1, RoundingMode.HALF_UP);
            BigDecimal dishProtein = food.getProteinPer100g().multiply(factor).setScale(1, RoundingMode.HALF_UP);
            BigDecimal dishCarb = food.getCarbPer100g().multiply(factor).setScale(1, RoundingMode.HALF_UP);
            BigDecimal dishFat = food.getFatPer100g().multiply(factor).setScale(1, RoundingMode.HALF_UP);

            // 1. Calorie proximity match (Weight: 0.45)
            double kcalDiffRatio = Math.abs(dishKcal.doubleValue() - targetMealKcal.doubleValue()) / Math.max(100.0, targetMealKcal.doubleValue());
            double kcalScore = Math.max(0.0, 1.0 - kcalDiffRatio);

            // 2. Protein alignment match (Weight: 0.35)
            double proteinRatio = Math.min(1.0, dishProtein.doubleValue() / Math.max(10.0, targetMealProtein.doubleValue()));

            // 3. Category match bonus (Weight: 0.20)
            double catBonus = 0.0;
            if (food.getCategory() != null && food.getCategory().equalsIgnoreCase(targetMealType)) {
                catBonus = 1.0;
            } else if ("SNACK".equalsIgnoreCase(targetMealType) && ("Breakfast".equalsIgnoreCase(food.getCategory()) || "ATOMIC_INGREDIENT".equalsIgnoreCase(food.getFoodType()))) {
                catBonus = 0.8;
            }

            // 4. Repeat food penalty (-0.40)
            double repeatPenalty = eatenFoodCodes.contains(food.getFoodCode()) ? 0.40 : 0.0;

            // 5. Goal specific modifiers
            double goalBonus = 0.0;
            if ("LOSE_WEIGHT".equalsIgnoreCase(user.getFitnessGoal())) {
                if (dishKcal.doubleValue() <= targetMealKcal.doubleValue() && dishProtein.doubleValue() >= 12.0) {
                    goalBonus += 0.15;
                }
            } else if ("GAIN_WEIGHT".equalsIgnoreCase(user.getFitnessGoal())) {
                if (dishProtein.doubleValue() >= 20.0) {
                    goalBonus += 0.20;
                }
            }

            double totalScore = (0.40 * kcalScore) + (0.35 * proteinRatio) + (0.15 * catBonus) + goalBonus - repeatPenalty;
            scoredList.add(new ScoredDish(food, dishKcal, dishProtein, dishCarb, dishFat, servingWeight, Math.max(0.05, totalScore)));
        }

        scoredList.sort((a, b) -> Double.compare(b.score, a.score));

        // Pick top 3 recommendations
        List<RecommendedDishItemDTO> topDishes = new ArrayList<>();
        int count = Math.min(3, scoredList.size());
        for (int i = 0; i < count; i++) {
            ScoredDish sd = scoredList.get(i);
            FoodDensity f = sd.food;
            BigDecimal volume = f.getServingVolumeCm3() != null ? f.getServingVolumeCm3() : sd.servingWeight.divide(f.getDensityGramsPerCm3(), 1, RoundingMode.HALF_UP);
            String note = String.format("Cung cấp %.0f kcal, %.1fg protein, lý tưởng cho %s",
                    sd.kcal.doubleValue(), sd.protein.doubleValue(), targetMealType.toLowerCase());

            topDishes.add(RecommendedDishItemDTO.builder()
                    .foodCode(f.getFoodCode())
                    .displayName(f.getDisplayName())
                    .category(f.getCategory())
                    .foodType(f.getFoodType())
                    .densityGramsPerCm3(f.getDensityGramsPerCm3())
                    .suggestedWeightG(sd.servingWeight)
                    .suggestedVolumeCm3(volume)
                    .estimatedKcal(sd.kcal)
                    .proteinG(sd.protein)
                    .carbG(sd.carb)
                    .fatG(sd.fat)
                    .emoji(f.getEmoji() != null ? f.getEmoji() : "🍲")
                    .iconType(f.getIconType() != null ? f.getIconType() : "salad")
                    .iconBgColor(f.getIconBgColor() != null ? f.getIconBgColor() : "#FFEFE6")
                    .iconColor(f.getIconColor() != null ? f.getIconColor() : "#F37340")
                    .matchScore(BigDecimal.valueOf(sd.score).setScale(2, RoundingMode.HALF_UP))
                    .suitabilityNote(note)
                    .build());
        }

        // Clinical reasoning explanation in Vietnamese
        String clinicalRationale = buildClinicalRationale(user, targetMealType, remainingKcal, targetMealProtein, topDishes);

        return MealRecommendationResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .targetMealType(targetMealType)
                .fitnessGoal(user.getFitnessGoal())
                .dailyCalorieGoal(user.getDailyCalorieGoal())
                .todayConsumedKcal(todayConsumedKcal)
                .remainingKcalToday(remainingKcal)
                .todayConsumedProteinG(consumedProtein)
                .todayConsumedCarbG(consumedCarb)
                .todayConsumedFatG(consumedFat)
                .targetMealKcal(targetMealKcal)
                .targetMealProteinG(targetMealProtein)
                .aiReasoningSummary(clinicalRationale)
                .recommendationSource("NUTRITION_CLINICAL_ENGINE")
                .recommendedDishes(topDishes)
                .build();
    }

    private String buildClinicalRationale(User user, String mealType, BigDecimal remainingKcal, BigDecimal targetProtein, List<RecommendedDishItemDTO> dishes) {
        String goalText = "Duy trì vóc dáng";
        if ("LOSE_WEIGHT".equalsIgnoreCase(user.getFitnessGoal())) goalText = "Giảm cân & Đốt mỡ";
        else if ("GAIN_WEIGHT".equalsIgnoreCase(user.getFitnessGoal())) goalText = "Tăng cơ nạc";

        String mealVn = "Bữa Tối";
        if ("BREAKFAST".equalsIgnoreCase(mealType)) mealVn = "Bữa Sáng";
        else if ("LUNCH".equalsIgnoreCase(mealType)) mealVn = "Bữa Trưa";
        else if ("SNACK".equalsIgnoreCase(mealType)) mealVn = "Bữa Phụ";

        String topDishName = dishes.isEmpty() ? "Món ăn thanh đạm" : dishes.get(0).getDisplayName();

        if (remainingKcal.compareTo(new BigDecimal("150")) <= 0) {
            return String.format("Bạn đã đạt hoặc sắp vượt hạn mức năng lượng hôm nay. Khuyến nghị %s thanh nhẹ giàu chất xơ như rau củ luộc hoặc salad để tạo cảm giác no mà không tích trữ thêm năng lượng dư thừa.", mealVn.toLowerCase());
        }

        return String.format("Theo mục tiêu %s, bạn còn lại khoảng %.0f kcal hôm nay. Khuyến nghị %s ưu tiên %s nhằm nạp khoảng %.1fg protein nạc, giúp tối ưu hóa tổng năng lượng và phục hồi cơ bắp hiệu quả.",
                goalText, remainingKcal.doubleValue(), mealVn.toLowerCase(), topDishName, targetProtein.doubleValue());
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue()).setScale(2, RoundingMode.HALF_UP);
        try {
            return new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static class ScoredDish {
        final FoodDensity food;
        final BigDecimal kcal;
        final BigDecimal protein;
        final BigDecimal carb;
        final BigDecimal fat;
        final BigDecimal servingWeight;
        final double score;

        ScoredDish(FoodDensity food, BigDecimal kcal, BigDecimal protein, BigDecimal carb, BigDecimal fat, BigDecimal servingWeight, double score) {
            this.food = food;
            this.kcal = kcal;
            this.protein = protein;
            this.carb = carb;
            this.fat = fat;
            this.servingWeight = servingWeight;
            this.score = score;
        }
    }
}
