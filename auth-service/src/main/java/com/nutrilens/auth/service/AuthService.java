package com.nutrilens.auth.service;

import com.nutrilens.auth.dto.AuthResponseDTO;
import com.nutrilens.auth.dto.LoginRequestDTO;
import com.nutrilens.auth.dto.RegisterRequestDTO;
import com.nutrilens.auth.dto.UpdateProfileRequestDTO;
import com.nutrilens.common.dto.response.UserSummaryDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO getCurrentUser(String username);
    AuthResponseDTO updateProfile(String username, UpdateProfileRequestDTO request);
    UserSummaryDTO getUserSummary(String username);
    UserSummaryDTO getUserSummaryById(Long id);
}
