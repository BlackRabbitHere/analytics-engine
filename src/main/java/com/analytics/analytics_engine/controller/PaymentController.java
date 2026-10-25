package com.analytics.analytics_engine.controller;


import com.analytics.analytics_engine.dto.PaymentRefundRequestDTO;
import com.analytics.analytics_engine.dto.PaymentRefundResponseDTO;
import com.analytics.analytics_engine.dto.PaymentRequestDTO;
import com.analytics.analytics_engine.dto.PaymentResponseDTO;
import com.analytics.analytics_engine.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/callback")
    public ResponseEntity<PaymentResponseDTO>completePayment(@Valid @RequestBody PaymentRequestDTO paymentRequestDTO){
        return new ResponseEntity<>(paymentService.completePayment(paymentRequestDTO),HttpStatus.OK);
    }


    @PostMapping("/init-refund")
    public ResponseEntity<PaymentRefundResponseDTO>refundPayment(@Valid @RequestBody PaymentRefundRequestDTO paymentRefundRequestDTO){
        return new ResponseEntity<>(paymentService.refundPayment(paymentRefundRequestDTO),HttpStatus.OK);
    }
}
