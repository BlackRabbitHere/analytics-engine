package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.PaymentRequestDTO;
import com.analytics.analytics_engine.dto.PaymentResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.Order;
import com.analytics.analytics_engine.model.Payment;
import com.analytics.analytics_engine.repository.OrderRepository;
import com.analytics.analytics_engine.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }


    @Transactional
    @Override
    public PaymentResponseDTO completePayment(PaymentRequestDTO payment) {
        Long orderId=payment.getOrderId();
        String providerReference=payment.getProviderReference()
                .strip()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s-_]+", "_") //convert the insider spaces , _ to _
                .replaceAll("^_|_$", "");

        Order order=orderRepository.findById(orderId).orElseThrow(
                ()->new APIException(ErrorCode.ORDER_NOT_FOUND)
        );

        BigDecimal orderAmount=order.getAmount();

        Payment completePayment=new Payment(order,providerReference,orderAmount);

        Payment completedPayment=paymentRepository.saveAndFlush(completePayment);
        order.markPaid(completedPayment.getCreatedAt());
        orderRepository.saveAndFlush(order);
        return PaymentResponseDTO.toResponse(completedPayment);
    }
}
