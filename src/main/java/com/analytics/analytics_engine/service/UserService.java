package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.UserRequestDTO;
import com.analytics.analytics_engine.dto.UserResponseDTO;


public interface UserService {
    UserResponseDTO createUser(UserRequestDTO userRequestDTO);
}
