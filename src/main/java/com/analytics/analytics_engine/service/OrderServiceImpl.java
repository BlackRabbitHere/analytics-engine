package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.OrderRequestDTO;
import com.analytics.analytics_engine.dto.OrderResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.Lead;
import com.analytics.analytics_engine.model.Order;
import com.analytics.analytics_engine.model.User;
import com.analytics.analytics_engine.repository.LeadRepository;
import com.analytics.analytics_engine.repository.OrderRepository;
import com.analytics.analytics_engine.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;


    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, LeadRepository leadRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
    }


    @Transactional
    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO newOrder) {
        Long userId=newOrder.getUserId();
        Long leadId=newOrder.getLeadId();
        BigDecimal newOrderAmount=newOrder.getOrderAmount();

        User user=userRepository.findById(userId).orElseThrow(
                ()-> new APIException(ErrorCode.USER_NOT_FOUND)
        );
        Lead lead=leadRepository.findById(leadId).orElseThrow(
                ()-> new APIException(ErrorCode.LEAD_NOT_FOUND)
        );
        boolean isLeadIsOfSameUser=lead.getUser().getId().equals(userId);

        if(!isLeadIsOfSameUser){
            throw  new APIException(ErrorCode.LEAD_USER_MISMATCH);
        }


        Order order=new Order(user,lead,newOrderAmount);
        Order createdOrder=orderRepository.saveAndFlush(order);

        return OrderResponseDTO.toResponse(createdOrder);
    }
}
