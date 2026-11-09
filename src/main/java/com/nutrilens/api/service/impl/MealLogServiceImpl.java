package com.nutrilens.api.service.impl;

import com.nutrilens.api.dto.request.CreateMealItemRequestDTO;
import com.nutrilens.api.dto.request.CreateMealLogRequestDTO;
import com.nutrilens.api.dto.response.MealItemResponseDTO;
import com.nutrilens.api.dto.response.MealLogResponseDTO;
import com.nutrilens.api.entity.FoodDensity;
import com.nutrilens.api.entity.MealItem;
import com.nutrilens.api.entity.MealLog;
import com.nutrilens.api.entity.User;
import com.nutrilens.api.exception.EntityNotFoundException;
import com.nutrilens.api.repository.FoodDensityRepository;
import com.nutrilens.api.repository.MealItemRepository;
import com.nutrilens.api.repository.MealLogRepository;
import com.nutrilens.api.repository.UserRepository;
import com.nutrilens.api.service.MealLogService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealLogServiceImpl implements MealLogService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MealLogServiceImpl.class);

    private final MealLogRepository mealLogRepository;
    private final MealItemRepository mealItemRepository;
    private final UserRepository userRepository;
    private final FoodDensityRepository foodDensityRepository;

    private static final BigDecimal RUNNING_MET = BigDecimal.valueOf(8.3);
    private static final BigDecimal WALKING_MET = BigDecimal.valueOf(3.8);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public MealLogServiceImpl(MealLogRepository mealLogRepository, MealItemRepository mealItemRepository,
                               UserRepository userRepository, FoodDensityRepository foodDensityRepository) {
        this.mealLogRepository = mealLogRepository;
        this.mealItemRepository = mealItemRepository;
        this.userRepository = userRepository;
        this.foodDensityRepository = foodDensityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealLogResponseDTO> getRecentMealLogs(Long userId, String username) {
        List<MealLog> logs;
        if (userId != null) {
            logs = mealLogRepository.findByUserIdOrderByLoggedAtDesc(userId);
        } else if (username != null && !username.trim().isEmpty()) {
            logs = mealLogRepository.findByUserUsernameOrderByLoggedAtDesc(username.trim());
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
        log.info("Creating new meal log: user='{}', mealType='{}', itemsCount={}",
                request.getUsername(), request.getMealType(),
                request.getItems() != null ? request.getItems().size() : 0);

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }
        if (user == null && request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user = userRepository.findByUsername(request.getUsername().trim()).orElse(null);
        }
        if (user == null) {
            String userIdent = request.getUsername() != null ? request.getUsername() : String.valueOf(request.getUserId());
            throw new EntityNotFoundException("User not found for meal log: '" + userIdent + "'. Please register or log in first.");
        }

        MealLog logEntity = new MealLog();
        logEntity.setUser(user);
        logEntity.setMealType(request.getMealType() != null ? request.getMealType().toUpperCase() : "LUNCH");
        LocalDateTime logTime = request.getLoggedAt() != null
                ? request.getLoggedAt()
                : LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        logEntity.setLoggedAt(logTime);
        logEntity.setTotalVolumeCm3(request.getTotalVolumeCm3() != null ? request.getTotalVolumeCm3() : BigDecimal.ZERO);
        logEntity.setTotalWeightG(request.getTotalWeightG() != null ? request.getTotalWeightG() : BigDecimal.ZERO);
        logEntity.setTotalKcal(request.getTotalKcal() != null ? request.getTotalKcal() : BigDecimal.ZERO);
        logEntity.setTotalProteinG(request.getTotalProteinG() != null ? request.getTotalProteinG() : BigDecimal.ZERO);
        logEntity.setTotalCarbG(request.getTotalCarbG() != null ? request.getTotalCarbG() : BigDecimal.ZERO);
        logEntity.setTotalFatG(request.getTotalFatG() != null ? request.getTotalFatG() : BigDecimal.ZERO);
        logEntity.setArSnapshotUrl(request.getArSnapshotUrl() != null ? request.getArSnapshotUrl() : "");
        logEntity.setUserNotes(request.getUserNotes() != null ? request.getUserNotes() : "AR Volumetric Scan");
        logEntity.setItems(new ArrayList<>());

        MealLog savedLog = mealLogRepository.save(logEntity);

        if (request.getItems() != null) {
            for (CreateMealItemRequestDTO itemDto : request.getItems()) {
                FoodDensity fd = null;
                if (itemDto.getFoodCode() != null) {
                    fd = foodDensityRepository.findByFoodCode(itemDto.getFoodCode()).orElse(null);
                }
                if (fd == null && !foodDensityRepository.findAll().isEmpty()) {
                    fd = foodDensityRepository.findAll().get(0);
                }

                MealItem item = new MealItem();
                item.setMealLog(savedLog);
                item.setFoodDensity(fd);
                item.setRawVolumeCm3(itemDto.getRawVolumeCm3() != null ? itemDto.getRawVolumeCm3() : BigDecimal.ZERO);
                item.setPortionRatio(itemDto.getPortionRatio() != null ? itemDto.getPortionRatio() : BigDecimal.ONE);
                item.setEffectiveVolumeCm3(itemDto.getEffectiveVolumeCm3() != null ? itemDto.getEffectiveVolumeCm3() : BigDecimal.ZERO);
                item.setEstimatedWeightG(itemDto.getEstimatedWeightG() != null ? itemDto.getEstimatedWeightG() : BigDecimal.ZERO);
                item.setCalculatedKcal(itemDto.getCalculatedKcal() != null ? itemDto.getCalculatedKcal() : BigDecimal.ZERO);
                item.setProteinG(itemDto.getProteinG() != null ? itemDto.getProteinG() : BigDecimal.ZERO);
                item.setCarbG(itemDto.getCarbG() != null ? itemDto.getCarbG() : BigDecimal.ZERO);
                item.setFatG(itemDto.getFatG() != null ? itemDto.getFatG() : BigDecimal.ZERO);
                item.setConfidenceScore(itemDto.getConfidenceScore() != null ? itemDto.getConfidenceScore() : new BigDecimal("0.950"));

                MealItem savedItem = mealItemRepository.save(item);
                savedLog.getItems().add(savedItem);
            }
        }

        return mapToDTO(savedLog);
    }

    private MealLogResponseDTO mapToDTO(MealLog log) {
        BigDecimal userWeight = (log.getUser() != null && log.getUser().getWeightKg() != null)
                ? log.getUser().getWeightKg() : BigDecimal.valueOf(65.0);

        int runMins = computeBurnMinutes(log.getTotalKcal(), RUNNING_MET, userWeight);
        int walkMins = computeBurnMinutes(log.getTotalKcal(), WALKING_MET, userWeight);

        List<MealItemResponseDTO> itemDTOs = log.getItems().stream().map(item -> {
            FoodDensity fd = item.getFoodDensity();
            return MealItemResponseDTO.builder()
                    .id(item.getId())
                    .foodDensityId(fd != null ? fd.getId() : null)
                    .foodCode(fd != null ? fd.getFoodCode() : "UNKNOWN")
                    .foodName(fd != null ? fd.getDisplayName() : "Unknown Dish")
                    .category(fd != null ? fd.getCategory() : "Meal")
                    .emoji(fd != null && fd.getEmoji() != null ? fd.getEmoji() : "🥗")
                    .iconType(fd != null && fd.getIconType() != null ? fd.getIconType() : "salad")
                    .iconBgColor(fd != null && fd.getIconBgColor() != null ? fd.getIconBgColor() : "#FFEFE6")
                    .iconColor(fd != null && fd.getIconColor() != null ? fd.getIconColor() : "#F37340")
                    .rawVolumeCm3(item.getRawVolumeCm3())
                    .portionRatio(item.getPortionRatio())
                    .effectiveVolumeCm3(item.getEffectiveVolumeCm3())
                    .estimatedWeightG(item.getEstimatedWeightG())
                    .calculatedKcal(item.getCalculatedKcal())
                    .proteinG(item.getProteinG())
                    .carbG(item.getCarbG())
                    .fatG(item.getFatG())
                    .confidenceScore(item.getConfidenceScore())
                    .build();
        }).collect(Collectors.toList());

        String displayTime = log.getLoggedAt() != null ? log.getLoggedAt().format(DATE_FORMATTER) : "";

        return MealLogResponseDTO.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUser() != null ? log.getUser().getUsername() : "Guest")
                .userFullName(log.getUser() != null ? log.getUser().getFullName() : "Guest User")
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

    private int computeBurnMinutes(BigDecimal totalKcal, BigDecimal met, BigDecimal userWeightKg) {
        if (totalKcal == null || totalKcal.compareTo(BigDecimal.ZERO) <= 0) return 0;
        BigDecimal calPerMinute = met.multiply(BigDecimal.valueOf(3.5)).multiply(userWeightKg)
                .divide(BigDecimal.valueOf(200), 4, RoundingMode.HALF_UP);
        if (calPerMinute.compareTo(BigDecimal.ZERO) <= 0) return 0;
        return totalKcal.divide(calPerMinute, 0, RoundingMode.CEILING).intValue();
    }
}
