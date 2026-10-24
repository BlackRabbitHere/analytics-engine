package com.analytics.analytics_engine.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


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

    @NotNull(message = "Order amount is required")
    @Positive(message = "Order Amount cannot be negative")
    @Digits(integer = 10,fraction = 2,message = "Amount can have upto 10 digits and 2 fraction digits")
    private BigDecimal orderAmount;
}
