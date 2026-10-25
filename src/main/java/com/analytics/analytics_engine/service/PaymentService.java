package com.analytics.analytics_engine.service;

import com.analytics.analytics_engine.dto.PaymentRequestDTO;
import com.analytics.analytics_engine.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO completePayment(PaymentRequestDTO payment);
}
