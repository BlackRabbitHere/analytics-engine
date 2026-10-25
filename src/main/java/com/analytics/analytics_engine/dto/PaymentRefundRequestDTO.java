package com.analytics.analytics_engine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequestDTO {
    @NotNull(message = "orderId is required")
    @Positive(message = "orderId cannot be negative")
    private Long orderId;

    @Size(max = 100,message = "provider Reference char limit upto 100 words")
    @NotBlank(message = "Provider reference cannot be blank")
    private String providerReference;
}
