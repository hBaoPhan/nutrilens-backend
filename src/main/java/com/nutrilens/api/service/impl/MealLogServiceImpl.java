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

    @PostConstruct
    public void seedInitialMealLogs() {
        if (mealLogRepository.count() == 0) {
            log.info("No meal logs found. Seeding initial AR meal logs for testing...");
            User user1 = userRepository.findByUsername("user1").orElse(null);
            if (user1 != null) {
                createSeedLog(user1, "BREAKFAST", LocalDateTime.now().minusHours(4),
                        "Bữa sáng Eat-Clean quét AR: Salad trứng & Dứa tráng miệng",
                        List.of("SALAD_EGG", "GREEN_VEG", "PINEAPPLE"));
                createSeedLog(user1, "LUNCH", LocalDateTime.now().minusDays(1).withHour(12).withMinute(15),
                        "Bữa trưa công sở nạp năng lượng: Cơm tấm sườn & Salad ức gà",
                        List.of("COM_TAM", "CHICKEN_SALAD"));
                createSeedLog(user1, "DINNER", LocalDateTime.now().minusDays(1).withHour(19).withMinute(0),
                        "Bữa tối nhẹ bụng giàu Omega-3: Cá hồi nướng măng tây",
                        List.of("SALMON_STEAK", "CANH_CHUA_CA_HOI"));
            }
        }
    }

    private void createSeedLog(User user, String mealType, LocalDateTime time, String notes, List<String> foodCodes) {
        MealLog logEntity = new MealLog();
        logEntity.setUser(user);
        logEntity.setMealType(mealType);
        logEntity.setLoggedAt(time);
        logEntity.setUserNotes(notes);
        logEntity.setArSnapshotUrl("https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600");

        BigDecimal totalVol = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalKcal = BigDecimal.ZERO;
        BigDecimal totalP = BigDecimal.ZERO;
        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal totalF = BigDecimal.ZERO;

        List<MealItem> items = new ArrayList<>();
        for (String code : foodCodes) {
            FoodDensity fd = foodDensityRepository.findByFoodCode(code).orElse(null);
            if (fd != null) {
                BigDecimal weight = fd.getServingWeightG() != null ? fd.getServingWeightG() : BigDecimal.valueOf(100);
                BigDecimal vol = fd.getServingVolumeCm3() != null ? fd.getServingVolumeCm3() : BigDecimal.valueOf(150);
                BigDecimal factor = weight.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

                BigDecimal kcal = fd.getKcalPer100g().multiply(factor).setScale(1, RoundingMode.HALF_UP);
                BigDecimal p = fd.getProteinPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                BigDecimal c = fd.getCarbPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                BigDecimal f = fd.getFatPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);

                MealItem item = MealItem.builder()
                        .mealLog(logEntity)
                        .foodDensity(fd)
                        .rawVolumeCm3(vol)
                        .portionRatio(BigDecimal.ONE)
                        .effectiveVolumeCm3(vol)
                        .estimatedWeightG(weight)
                        .calculatedKcal(kcal)
                        .proteinG(p)
                        .carbG(c)
                        .fatG(f)
                        .confidenceScore(new BigDecimal("0.965"))
                        .build();

                items.add(item);
                totalVol = totalVol.add(vol);
                totalWeight = totalWeight.add(weight);
                totalKcal = totalKcal.add(kcal);
                totalP = totalP.add(p);
                totalC = totalC.add(c);
                totalF = totalF.add(f);
            }
        }

        logEntity.setTotalVolumeCm3(totalVol);
        logEntity.setTotalWeightG(totalWeight);
        logEntity.setTotalKcal(totalKcal);
        logEntity.setTotalProteinG(totalP);
        logEntity.setTotalCarbG(totalC);
        logEntity.setTotalFatG(totalF);
        logEntity.setItems(items);

        mealLogRepository.save(logEntity);
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
            user = userRepository.findByUsername("admin").orElseGet(() ->
                    userRepository.findAll().stream().findFirst().orElse(null));
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
