package com.nutrilens.api.service;

import com.nutrilens.api.dto.request.CreateFoodItemRequestDTO;
import com.nutrilens.api.dto.response.AdminSystemStatsResponseDTO;
import com.nutrilens.api.dto.response.FoodItemResponseDTO;

public interface AdminService {

    AdminSystemStatsResponseDTO getSystemStats();

    FoodItemResponseDTO createFood(CreateFoodItemRequestDTO request);

    FoodItemResponseDTO updateFood(Long id, CreateFoodItemRequestDTO request);

    void deleteFood(Long id);
}
