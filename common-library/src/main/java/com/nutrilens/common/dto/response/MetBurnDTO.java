package com.nutrilens.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetBurnDTO {
    private Double runningMinutes;
    private Double walkingMinutes;
    private Double cyclingMinutes;
    private Double caloriesBurnedPerMinute;
}
