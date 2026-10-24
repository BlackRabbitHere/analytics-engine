package com.analytics.analytics_engine.controller;


import com.analytics.analytics_engine.dto.OrderRequestDTO;
import com.analytics.analytics_engine.dto.OrderResponseDTO;
import com.analytics.analytics_engine.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO newOrder){

        return new ResponseEntity<>(orderService.createOrder(newOrder), HttpStatus.CREATED);
    }
}
