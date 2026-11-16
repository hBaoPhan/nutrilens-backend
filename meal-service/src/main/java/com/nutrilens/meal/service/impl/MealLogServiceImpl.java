package com.nutrilens.meal.service.impl;

import com.nutrilens.common.dto.response.FoodItemDTO;
import com.nutrilens.common.dto.response.UserSummaryDTO;
import com.nutrilens.common.exception.EntityNotFoundException;
import com.nutrilens.meal.client.AuthServiceClient;
import com.nutrilens.meal.client.NutritionServiceClient;
import com.nutrilens.meal.dto.CreateMealItemRequestDTO;
import com.nutrilens.meal.dto.CreateMealLogRequestDTO;
import com.nutrilens.meal.dto.MealItemResponseDTO;
import com.nutrilens.meal.dto.MealLogResponseDTO;
import com.nutrilens.meal.entity.MealItem;
import com.nutrilens.meal.entity.MealLog;
import com.nutrilens.meal.repository.MealItemRepository;
import com.nutrilens.meal.repository.MealLogRepository;
import com.nutrilens.meal.service.MealLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealLogServiceImpl implements MealLogService {

    private final MealLogRepository mealLogRepository;
    private final MealItemRepository mealItemRepository;
    private final AuthServiceClient authServiceClient;
    private final NutritionServiceClient nutritionServiceClient;

    private static final BigDecimal RUNNING_MET = BigDecimal.valueOf(8.3);
    private static final BigDecimal WALKING_MET = BigDecimal.valueOf(3.8);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Override
    @Transactional(readOnly = true)
    public List<MealLogResponseDTO> getRecentMealLogs(Long userId, String username) {
        List<MealLog> logs;
        if (userId != null && userId > 0) {
            logs = mealLogRepository.findByUserIdOrderByLoggedAtDesc(userId);
        } else if (username != null && !username.trim().isEmpty()) {
            logs = mealLogRepository.findByUsernameOrderByLoggedAtDesc(username.trim());
        } else {
            logs = mealLogRepository.findAllByOrderByLoggedAtDesc();
        }

        return logs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealLogResponseDTO> getFilteredMealLogs(String username, String date, String mealType) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (date != null && !date.trim().isEmpty()) {
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(date.trim());
                startDate = localDate.atStartOfDay();
                endDate = localDate.plusDays(1).atStartOfDay();
            } catch (Exception e) {
                log.warn("Invalid date format: {}, expected YYYY-MM-DD", date);
            }
        }
        String cleanUsername = (username != null && !username.trim().isEmpty()) ? username.trim() : null;
        String cleanMealType = (mealType != null && !mealType.trim().isEmpty() && !mealType.equalsIgnoreCase("ALL")) ? mealType.trim() : null;

        List<MealLog> logs = mealLogRepository.findFiltered(cleanUsername, startDate, endDate, cleanMealType);
        return logs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MealLogResponseDTO getMealLogById(Long id) {
        MealLog mealLog = mealLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal log not found with ID: " + id));
        return mapToDTO(mealLog);
    }

    @Override
    @Transactional
    public MealLogResponseDTO createMealLog(CreateMealLogRequestDTO request) {
        log.info("[MEAL SERVICE] Creating new meal log for user: {}, mealType: {}", request.getUsername(), request.getMealType());

        // 1. Resolve user profile via AuthServiceClient
        String targetUsername = request.getUsername();
        Long targetUserId = request.getUserId();
        UserSummaryDTO userSummary = null;

        try {
            if (targetUsername != null && !targetUsername.isBlank()) {
                userSummary = authServiceClient.getUserSummary(targetUsername);
                targetUserId = userSummary.getId();
            } else if (targetUserId != null) {
                userSummary = authServiceClient.getUserSummaryById(targetUserId);
                targetUsername = userSummary.getUsername();
            }
        } catch (Exception e) {
            log.warn("[MEAL SERVICE] Failed to fetch user from Auth Service. Using fallback.", e);
        }

        if (targetUserId == null) targetUserId = 1L;
        if (targetUsername == null || targetUsername.isBlank()) targetUsername = "admin";

        LocalDateTime logTime = request.getLoggedAt() != null
                ? request.getLoggedAt()
                : LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        MealLog logEntity = MealLog.builder()
                .userId(targetUserId)
                .username(targetUsername)
                .mealType(request.getMealType() != null ? request.getMealType().toUpperCase() : "LUNCH")
                .loggedAt(logTime)
                .totalVolumeCm3(request.getTotalVolumeCm3() != null ? request.getTotalVolumeCm3() : BigDecimal.ZERO)
                .totalWeightG(request.getTotalWeightG() != null ? request.getTotalWeightG() : BigDecimal.ZERO)
                .totalKcal(request.getTotalKcal() != null ? request.getTotalKcal() : BigDecimal.ZERO)
                .totalProteinG(request.getTotalProteinG() != null ? request.getTotalProteinG() : BigDecimal.ZERO)
                .totalCarbG(request.getTotalCarbG() != null ? request.getTotalCarbG() : BigDecimal.ZERO)
                .totalFatG(request.getTotalFatG() != null ? request.getTotalFatG() : BigDecimal.ZERO)
                .arSnapshotUrl(request.getArSnapshotUrl() != null ? request.getArSnapshotUrl() : "")
                .userNotes(request.getUserNotes() != null ? request.getUserNotes() : "AR Volumetric Scan")
                .items(new ArrayList<>())
                .build();

        MealLog savedLog = mealLogRepository.save(logEntity);

        if (request.getItems() != null) {
            for (CreateMealItemRequestDTO itemDto : request.getItems()) {
                Long foodDensityId = 1L;
                String foodName = itemDto.getFoodName();

                try {
                    FoodItemDTO foodItem = nutritionServiceClient.getFoodByCode(itemDto.getFoodCode());
                    if (foodItem != null) {
                        foodDensityId = foodItem.getId() != null ? foodItem.getId() : 1L;
                        if (foodName == null || foodName.isBlank()) {
                            foodName = foodItem.getDisplayName();
                        }
                    }
                } catch (Exception e) {
                    log.warn("[MEAL SERVICE] Failed to fetch food item for code: {}", itemDto.getFoodCode());
                }

                if (foodName == null || foodName.isBlank()) {
                    foodName = itemDto.getFoodCode();
                }

                MealItem item = MealItem.builder()
                        .mealLog(savedLog)
                        .foodDensityId(foodDensityId)
                        .foodCode(itemDto.getFoodCode() != null ? itemDto.getFoodCode() : "UNKNOWN")
                        .foodName(foodName)
                        .rawVolumeCm3(itemDto.getRawVolumeCm3() != null ? itemDto.getRawVolumeCm3() : BigDecimal.ZERO)
                        .portionRatio(itemDto.getPortionRatio() != null ? itemDto.getPortionRatio() : BigDecimal.ONE)
                        .effectiveVolumeCm3(itemDto.getEffectiveVolumeCm3() != null ? itemDto.getEffectiveVolumeCm3() : BigDecimal.ZERO)
                        .estimatedWeightG(itemDto.getEstimatedWeightG() != null ? itemDto.getEstimatedWeightG() : BigDecimal.ZERO)
                        .calculatedKcal(itemDto.getCalculatedKcal() != null ? itemDto.getCalculatedKcal() : BigDecimal.ZERO)
                        .proteinG(itemDto.getProteinG() != null ? itemDto.getProteinG() : BigDecimal.ZERO)
                        .carbG(itemDto.getCarbG() != null ? itemDto.getCarbG() : BigDecimal.ZERO)
                        .fatG(itemDto.getFatG() != null ? itemDto.getFatG() : BigDecimal.ZERO)
                        .confidenceScore(itemDto.getConfidenceScore() != null ? itemDto.getConfidenceScore() : new BigDecimal("0.950"))
                        .build();

                MealItem savedItem = mealItemRepository.save(item);
                savedLog.getItems().add(savedItem);
            }
        }

        return mapToDTO(savedLog);
    }

    private MealLogResponseDTO mapToDTO(MealLog log) {
        BigDecimal userWeight = BigDecimal.valueOf(65.0);
        int runMins = computeBurnMinutes(log.getTotalKcal(), RUNNING_MET, userWeight);
        int walkMins = computeBurnMinutes(log.getTotalKcal(), WALKING_MET, userWeight);

        List<MealItemResponseDTO> itemDTOs = log.getItems().stream().map(item ->
                MealItemResponseDTO.builder()
                        .id(item.getId())
                        .foodDensityId(item.getFoodDensityId())
                        .foodCode(item.getFoodCode())
                        .foodName(item.getFoodName())
                        .emoji("🥗")
                        .rawVolumeCm3(item.getRawVolumeCm3())
                        .portionRatio(item.getPortionRatio())
                        .effectiveVolumeCm3(item.getEffectiveVolumeCm3())
                        .estimatedWeightG(item.getEstimatedWeightG())
                        .calculatedKcal(item.getCalculatedKcal())
                        .proteinG(item.getProteinG())
                        .carbG(item.getCarbG())
                        .fatG(item.getFatG())
                        .confidenceScore(item.getConfidenceScore())
                        .build()
        ).collect(Collectors.toList());

        String displayTime = log.getLoggedAt() != null ? log.getLoggedAt().format(DATE_FORMATTER) : "";

        return MealLogResponseDTO.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .userFullName(log.getUsername())
                .mealType(log.getMealType())
                .loggedAt(log.getLoggedAt())
                .loggedAtDisplay(displayTime)
                .totalVolumeCm3(log.getTotalVolumeCm3())
                .totalWeightG(log.getTotalWeightG())
                .totalKcal(log.getTotalKcal())
                .totalProteinG(log.getTotalProteinG())
                .totalCarbG(log.getTotalCarbG())
                .totalFatG(log.getTotalFatG())
                .arSnapshotUrl(log.getArSnapshotUrl())
                .userNotes(log.getUserNotes())
                .runningMinutesNeeded(runMins)
                .walkingMinutesNeeded(walkMins)
                .items(itemDTOs)
                .build();
    }

    private int computeBurnMinutes(BigDecimal calories, BigDecimal met, BigDecimal userWeight) {
        if (calories == null || calories.compareTo(BigDecimal.ZERO) <= 0) return 0;
        BigDecimal calPerMin = met.multiply(BigDecimal.valueOf(3.5)).multiply(userWeight).divide(BigDecimal.valueOf(200), 4, RoundingMode.HALF_UP);
        if (calPerMin.compareTo(BigDecimal.ZERO) <= 0) return 0;
        return calories.divide(calPerMin, 0, RoundingMode.HALF_UP).intValue();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTodayCalories(String username) {
        if (username == null || username.isBlank()) return BigDecimal.ZERO;
        java.time.LocalDate today = java.time.LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<MealLog> logs = mealLogRepository.findByUsernameAndLoggedAtBetween(username.trim(), start, end);
        return logs.stream()
                .map(MealLog::getTotalKcal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
