package com.analytics.analytics_engine.dto;


import com.analytics.analytics_engine.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDTO {

    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private BigDecimal totalOrderAmount;
    private Integer quantity;

    public static OrderItemResponseDTO toResponse(OrderItem orderItem){
        return new OrderItemResponseDTO(
                orderItem.getId(),
                orderItem.getOrder().getId(),
                orderItem.getProduct().getId(),
                orderItem.getProductName(),
                orderItem.getLineTotal(),
                orderItem.getQuantity()
        );
    }
}
