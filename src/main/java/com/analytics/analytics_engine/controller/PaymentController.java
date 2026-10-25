package com.analytics.analytics_engine.controller;


import com.analytics.analytics_engine.dto.PaymentRequestDTO;
import com.analytics.analytics_engine.dto.PaymentResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/payments")
public class PaymentController {


    @PostMapping("/compelete-payment")
    public ResponseEntity<PaymentResponseDTO>completePayment(@Valid @RequestBody PaymentRequestDTO){
        return
    }
}
