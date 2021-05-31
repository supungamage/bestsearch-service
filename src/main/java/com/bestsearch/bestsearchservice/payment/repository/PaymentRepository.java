package com.bestsearch.bestsearchservice.payment.repository;

import com.bestsearch.bestsearchservice.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
