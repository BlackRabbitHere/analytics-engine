package com.analytics.analytics_engine.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeadRequestDTO {

    @Positive(message = "userId cannot be negative")
    @NotNull(message = "user is required")
    private Long userId;

    @NotBlank(message = "source is required")
    @Size(max = 100,message = "Source cannot exceed more than 100 character")
    @NotNull
    private String source;
}
