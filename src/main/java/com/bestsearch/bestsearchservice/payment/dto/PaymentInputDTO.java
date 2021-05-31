package com.bestsearch.bestsearchservice.payment.dto;

import com.bestsearch.bestsearchservice.order.model.enums.PaymentPreference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInputDTO {
    private long orderId;
    private String orderRef;
    private BigDecimal totalAmount;
    private Long organizationId;
    private LocalDateTime paymentDate;
    private PaymentPreference paymentPreference;
}
