package com.analytics.analytics_engine.dto;

import com.analytics.analytics_engine.model.Order;
import com.analytics.analytics_engine.model.Payment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {

    private Long paymentId;

    private Long orderId;

    private String providerReference;

    private BigDecimal amount;

    private String status;

    private Instant createdAt;

    private Instant refundedAt;

    private String orderStatus;

    public static PaymentResponseDTO toResponse(Payment payment){
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getProviderReference(),
                payment.getAmount(),
                payment.getStatus().toString(),
                payment.getCreatedAt(),
                payment.getRefundedAt(),
                payment.getOrder().getStatus().toString()
        );
    }
}
