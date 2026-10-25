package com.analytics.analytics_engine.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "orderId is required")
    @Positive(message = "orderId cannot be negative")
    private Long orderId;

    @Size(max = 100,message = "provider Reference char limit upto 100 words")
    @NotBlank(message = "Provider reference cannot be blank")
    private String providerReference;


    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount cannot be negative")
    @Digits(integer = 10,fraction = 2,message = "Payment amount cannot exceed 999999999.99")
    private BigDecimal paymentAmount;

}
