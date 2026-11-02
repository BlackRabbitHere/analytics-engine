package com.analytics.analytics_engine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    @NotNull(message = "User Id is required")
    @Positive(message = "User Id cannot be negative")
    private Long userId;

    @NotNull(message = "Lead Id is required")
    @Positive(message = "Lead Id cannot be negative")
    private Long leadId;

    @NotEmpty(message = "At least one order item is required")
    @Valid
    private List<@NotNull OrderItemRequestDTO> items;
}
