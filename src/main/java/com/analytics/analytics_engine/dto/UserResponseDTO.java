package com.analytics.analytics_engine.dto;

import com.analytics.analytics_engine.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.Instant;


@Getter
public class UserResponseDTO {
    public UserResponseDTO(Long userId, String userName, Instant createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
    }


    private Long userId;
    @NotBlank
    private String userName;
    private Instant createdAt;

    public static UserResponseDTO toResponse(User user){
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt()
        );
    }
}
