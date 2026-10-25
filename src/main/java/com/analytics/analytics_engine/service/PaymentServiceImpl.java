package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.PaymentRefundRequestDTO;
import com.analytics.analytics_engine.dto.PaymentRefundResponseDTO;
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
import java.time.Instant;
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
    public PaymentResponseDTO completePayment(PaymentRequestDTO request) {
        Long orderId = request.getOrderId();
        String reference = request.getProviderReference();
        BigDecimal amount = request.getPaymentAmount();

        if (reference == null
                || reference.isBlank()
                || reference.length() > 100
                || amount == null
                || amount.signum() <= 0) {
            throw new APIException(ErrorCode.VALIDATION_FAILED);
        }

        // First database operation: acquire the order lock.
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new APIException(
                        ErrorCode.ORDER_NOT_FOUND
                ));

        // A reference identifies one particular payment.
        Optional<Payment> paymentWithReference =
                paymentRepository.findByProviderReference(reference);

        if (paymentWithReference.isPresent()) {
            Payment existing = paymentWithReference.get();

            boolean sameOrder =
                    existing.getOrder().getId().equals(orderId);

            boolean sameReference =
                    existing.getProviderReference().equals(reference);

            boolean sameAmount =
                    existing.getAmount().compareTo(amount) == 0;

            if (!sameOrder || !sameReference || !sameAmount) {
                throw new APIException(ErrorCode.PAYMENT_CONFLICT);
            }

            // Includes an identical replay after a refund.
            return PaymentResponseDTO.toResponse(existing);
        }

        // This order cannot receive a second payment reference.
        if (paymentRepository.findByOrder_Id(orderId).isPresent()) {
            throw new APIException(ErrorCode.PAYMENT_CONFLICT);
        }

        if (order.getStatus() != Order.Status.PENDING) {
            throw new APIException(ErrorCode.ORDER_NOT_PAYABLE);
        }

        if (order.getAmount().compareTo(amount) != 0) {
            throw new APIException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment completedPayment = paymentRepository.saveAndFlush(
                new Payment(order, reference, amount)
        );

        order.markPaid(completedPayment.getCreatedAt());

        Lead lead = order.getLead();
        lead.setStatus(Lead.Status.CONVERTED);

        return PaymentResponseDTO.toResponse(completedPayment);
    }

    @Transactional
    @Override
    public PaymentRefundResponseDTO refundPayment(PaymentRefundRequestDTO payment) {
        Long orderId=payment.getOrderId();
        String providerReference=payment.getProviderReference();

        if(providerReference==null
            || providerReference.isBlank()
            || providerReference.length()>100
        ){
            throw new APIException(ErrorCode.VALIDATION_FAILED);
        }


        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(
                ()-> new APIException(ErrorCode.ORDER_NOT_FOUND)
        );

        Optional<Payment> existingPayment=paymentRepository.findByProviderReferenceForUpdate(providerReference);

        if(existingPayment.isPresent()){
            boolean isOrderStatusPaid=order.getStatus().equals(Order.Status.PAID);
            boolean isPaymentStatusSuccess=existingPayment.get().getStatus().equals(Payment.Status.SUCCESS);

            if(!isOrderStatusPaid){
                throw new APIException(ErrorCode.ORDER_CANNOT_REFUNDED);
            }

            if(!isPaymentStatusSuccess){
                throw new APIException(ErrorCode.PAYMENT_CANNOT_REFUNDED);
            }

            Instant refundedAt=Instant.now();
            order.refund(refundedAt);
            existingPayment.get().refund(refundedAt);
            return PaymentRefundResponseDTO.toResponse(existingPayment.get());
        }else{
            throw new APIException(ErrorCode.PROVIDER_REFERENCE_MISMATCH);
        }

    }
}
