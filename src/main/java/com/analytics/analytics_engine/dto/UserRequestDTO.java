package com.analytics.analytics_engine.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    @NotBlank(message = "Username is required.")
    @Size(max = 255, message = "Username must be at most 255 characters.")
    private String username;
}
