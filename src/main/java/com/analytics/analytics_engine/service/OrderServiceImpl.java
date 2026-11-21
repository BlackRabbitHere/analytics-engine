package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.OrderItemRequestDTO;
import com.analytics.analytics_engine.dto.OrderItemResponseDTO;
import com.analytics.analytics_engine.dto.OrderRequestDTO;
import com.analytics.analytics_engine.dto.OrderResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.*;
import com.analytics.analytics_engine.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserActivityService userActivityService;


    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, LeadRepository leadRepository, ProductRepository productRepository, OrderItemRepository orderItemRepository, UserActivityService userActivityService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.userActivityService = userActivityService;
    }


    @Transactional
    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO newOrder) {
        Long userId=newOrder.getUserId();
        Long leadId=newOrder.getLeadId();

        List<OrderItemRequestDTO> orderItems=newOrder.getItems();
        BigDecimal totalPrice=BigDecimal.ZERO;
        for(OrderItemRequestDTO orderItem:orderItems){
            Product product=productRepository.getProductById(orderItem.getProductId())
                    .orElseThrow(()->new APIException(ErrorCode.PRODUCT_NOT_FOUND));
            Integer quantity=orderItem.getQuantity();
            totalPrice=product.getPrice().multiply(new BigDecimal(quantity)).add(totalPrice);
        }


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


        Order order=new Order(user,lead,totalPrice);
        Order createdOrder=orderRepository.saveAndFlush(order);

        List<OrderItem> items=new ArrayList<>();

        for(OrderItemRequestDTO requested:newOrder.getItems()){
            Product product = productRepository.getProductById(requested.getProductId()).orElseThrow(
                    ()->new APIException(ErrorCode.PRODUCT_NOT_FOUND)
            );

            items.add(new OrderItem(
                    createdOrder,
                    product,
                    requested.getQuantity()
            ));
        }

        List<OrderItem> savedItems=orderItemRepository.saveAllAndFlush(items);

        userActivityService.recordServerEvent(
                UserActivity.EventType.ORDER_CREATED,
                createdOrder.getUser(),
                createdOrder.getLead(),
                createdOrder,
                null,
                createdOrder.getCreatedAt()
        );

        List<OrderItemResponseDTO> savedOrderItems=savedItems.stream()
                .map(OrderItemResponseDTO::toResponse)
                .toList();

        return OrderResponseDTO.toResponse(createdOrder,savedOrderItems);
    }

    @Override
    public OrderResponseDTO getOrderByOrderId(Long orderId) {
        Order order= orderRepository.getOrderById(orderId)
                .orElseThrow(()-> new APIException(
                ErrorCode.ORDER_NOT_FOUND));
        List<OrderItem>orderItems=orderItemRepository.findByOrder_IdOrderByIdAsc(orderId);
        List<OrderItemResponseDTO>orderItemResponseDTOS=orderItems.stream()
                .map(OrderItemResponseDTO::toResponse)
                .toList();
        return OrderResponseDTO.toResponse(order,orderItemResponseDTOS);
    }
}
