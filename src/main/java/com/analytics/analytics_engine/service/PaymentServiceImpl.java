package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.PaymentRequestDTO;
import com.analytics.analytics_engine.dto.PaymentResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.Lead;
import com.analytics.analytics_engine.model.Order;
import com.analytics.analytics_engine.model.Payment;
import com.analytics.analytics_engine.repository.LeadRepository;
import com.analytics.analytics_engine.repository.OrderRepository;
import com.analytics.analytics_engine.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final LeadRepository leadRepository;

    public PaymentServiceImpl(OrderRepository orderRepository, PaymentRepository paymentRepository, LeadRepository leadRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.leadRepository = leadRepository;
    }


    @Transactional
    @Override
    public PaymentResponseDTO completePayment(PaymentRequestDTO payment) {

        Long orderId=payment.getOrderId();
        String providerReference=payment.getProviderReference()
                .strip()
                .toLowerCase(Locale.ROOT)
                .replaceAll("^_|_$", "");
        BigDecimal paymentAmount=payment.getPaymentAmount();

        Optional<Payment> existingPayment=paymentRepository.findByOrder_Id(orderId);
        if(existingPayment.isPresent()){
            if(providerReference.equals(existingPayment.get().getProviderReference())) {
                return PaymentResponseDTO.toResponse(existingPayment.get());
            }else {
                throw  new APIException(ErrorCode.PROVIDER_REFERENCE_MISMATCH);
            }
        }

        if(providerReference.isBlank() || providerReference.length() > 100){
            throw new APIException(ErrorCode.VALIDATION_FAILED);
        }

        Order order=orderRepository.findById(orderId).orElseThrow(
                ()->new APIException(ErrorCode.ORDER_NOT_FOUND)
        );


        Payment completePayment=new Payment(order,providerReference,paymentAmount);

        Payment completedPayment=paymentRepository.saveAndFlush(completePayment);
        order.markPaid(completedPayment.getCreatedAt());
        Order updatedOrder=orderRepository.saveAndFlush(order);
        Long leadId=updatedOrder.getLead().getId();
        Lead lead=leadRepository.findById(leadId).orElseThrow();
        lead.setStatus(Lead.Status.CONVERTED);
        leadRepository.saveAndFlush(lead);
        return PaymentResponseDTO.toResponse(completedPayment);
    }
}
