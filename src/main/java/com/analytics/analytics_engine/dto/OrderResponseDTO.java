package com.analytics.analytics_engine.dto;

import com.analytics.analytics_engine.model.Order;
import com.analytics.analytics_engine.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private Long userId;

    private Long leadId;

    private BigDecimal amount;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant paidAt;

    private Instant refundedAt;

    private List<OrderItemResponseDTO> items;

    public static OrderResponseDTO toResponse(Order order, List<OrderItemResponseDTO>orderItems){
        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getLead().getId(),
                order.getAmount(),
                order.getStatus().toString(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPaidAt(),
                order.getRefundedAt(),
                orderItems);
    }
}
