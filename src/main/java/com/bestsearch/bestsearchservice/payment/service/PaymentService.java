package com.bestsearch.bestsearchservice.payment.service;

import com.bestsearch.bestsearchservice.payment.dto.PaymentInputDTO;
import com.bestsearch.bestsearchservice.payment.dto.PaymentOutputDTO;

public interface PaymentService {

    public PaymentOutputDTO doPayment(PaymentInputDTO paymentInputDTO);
}
