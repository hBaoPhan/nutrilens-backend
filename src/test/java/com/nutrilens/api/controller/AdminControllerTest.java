package com.nutrilens.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrilens.api.config.SecurityConfig;
import com.nutrilens.api.dto.request.CreateFoodItemRequestDTO;
import com.nutrilens.api.dto.response.AdminSystemStatsResponseDTO;
import com.nutrilens.api.dto.response.FoodItemResponseDTO;
import com.nutrilens.api.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("GET /api/v1/admin/stats returns 200 with system telemetry")
    void testGetSystemStats() throws Exception {
        AdminSystemStatsResponseDTO stats = AdminSystemStatsResponseDTO.builder()
                .totalFoods(25)
                .totalMealLogs(120)
                .totalUsers(45)
                .totalTips(15)
                .averageKcalPerMeal(new BigDecimal("550.5"))
                .springBootStatus("UP")
                .aiServiceStatus("UP")
                .databaseStatus("UP")
                .build();

        when(adminService.getSystemStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFoods").value(25))
                .andExpect(jsonPath("$.totalMealLogs").value(120))
                .andExpect(jsonPath("$.springBootStatus").value("UP"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/foods creates food successfully and returns 201 Created")
    void testCreateFood() throws Exception {
        CreateFoodItemRequestDTO request = new CreateFoodItemRequestDTO();
        request.setFoodCode("VIET_BUN_CHA");
        request.setDisplayName("Bún Chả Hà Nội");
        request.setCategory("Lunch");
        request.setDensityGramsPerCm3(new BigDecimal("0.75"));
        request.setKcalPer100g(new BigDecimal("145.0"));
        request.setProteinPer100g(new BigDecimal("9.5"));
        request.setCarbPer100g(new BigDecimal("18.0"));
        request.setFatPer100g(new BigDecimal("4.5"));
        request.setEmoji("🍜");

        FoodItemResponseDTO response = FoodItemResponseDTO.builder()
                .id(10L)
                .foodCode("VIET_BUN_CHA")
                .name("Bún Chả Hà Nội")
                .category("Lunch")
                .kcal(new BigDecimal("145"))
                .emoji("🍜")
                .build();

        when(adminService.createFood(any(CreateFoodItemRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.foodCode").value("VIET_BUN_CHA"))
                .andExpect(jsonPath("$.name").value("Bún Chả Hà Nội"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/foods with invalid payload returns 400 Bad Request")
    void testCreateFoodValidationFailure() throws Exception {
        CreateFoodItemRequestDTO invalidRequest = new CreateFoodItemRequestDTO();
        // Missing foodCode, displayName, densityGramsPerCm3, etc.

        mockMvc.perform(post("/api/v1/admin/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/foods/{id} returns 204 No Content")
    void testDeleteFood() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/foods/1"))
                .andExpect(status().isNoContent());
    }
}
