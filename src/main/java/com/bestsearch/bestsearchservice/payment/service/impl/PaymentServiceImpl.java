package com.bestsearch.bestsearchservice.payment.service.impl;

import com.bestsearch.bestsearchservice.payment.dto.PaymentInputDTO;
import com.bestsearch.bestsearchservice.payment.dto.PaymentOutputDTO;
import com.bestsearch.bestsearchservice.payment.model.Payment;
import com.bestsearch.bestsearchservice.payment.repository.PaymentRepository;
import com.bestsearch.bestsearchservice.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(final PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentOutputDTO doPayment(PaymentInputDTO paymentInputDTO) {
        if(Objects.isNull(paymentInputDTO) || Objects.isNull(paymentInputDTO.getOrderId())) {
            throw new IllegalArgumentException("Mandatory fields empty");
        }

        return paymentRepository.save(Payment.builder().orderId(paymentInputDTO.getOrderId())
                .orderRef(paymentInputDTO.getOrderRef())
                .organizationId(paymentInputDTO.getOrganizationId())
                .paymentPreference(paymentInputDTO.getPaymentPreference())
                .totalAmount(paymentInputDTO.getTotalAmount())
                .paymentDate(LocalDateTime.now())
                .build()).viewAsPaymentOutputDTO();
    }
}
