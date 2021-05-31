package com.bestsearch.bestsearchservice.payment.controller;

import com.bestsearch.bestsearchservice.payment.dto.PaymentInputDTO;
import com.bestsearch.bestsearchservice.payment.dto.PaymentOutputDTO;
import com.bestsearch.bestsearchservice.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(final PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentOutputDTO> doPayment(@RequestBody PaymentInputDTO paymentInputDTO) {
        return ResponseEntity.ok(this.paymentService.doPayment(paymentInputDTO));
    }
}
