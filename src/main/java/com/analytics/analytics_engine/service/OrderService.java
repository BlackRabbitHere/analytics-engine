package com.analytics.analytics_engine.service;

import com.analytics.analytics_engine.dto.OrderRequestDTO;
import com.analytics.analytics_engine.dto.OrderResponseDTO;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO newOrder);
}
