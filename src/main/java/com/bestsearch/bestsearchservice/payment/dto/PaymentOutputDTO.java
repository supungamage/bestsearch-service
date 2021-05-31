package com.bestsearch.bestsearchservice.payment.dto;

import com.bestsearch.bestsearchservice.order.model.enums.PaymentPreference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PaymentOutputDTO {
    private Long id;
    private long orderId;
    private String orderRef;
    private BigDecimal totalAmount;
    private Long organizationId;
    private LocalDateTime paymentDate;
    private PaymentPreference paymentPreference;
}
