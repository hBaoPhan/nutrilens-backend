package com.nutrilens.api.controller;

import com.nutrilens.api.dto.request.CreateFoodItemRequestDTO;
import com.nutrilens.api.dto.response.AdminSystemStatsResponseDTO;
import com.nutrilens.api.dto.response.FoodItemResponseDTO;
import com.nutrilens.api.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminSystemStatsResponseDTO> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @PostMapping("/foods")
    public ResponseEntity<FoodItemResponseDTO> createFood(@Valid @RequestBody CreateFoodItemRequestDTO request) {
        FoodItemResponseDTO created = adminService.createFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/foods/{id}")
    public ResponseEntity<FoodItemResponseDTO> updateFood(
            @PathVariable Long id,
            @Valid @RequestBody CreateFoodItemRequestDTO request) {
        FoodItemResponseDTO updated = adminService.updateFood(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/foods/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        adminService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}
