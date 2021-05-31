package com.bestsearch.bestsearchservice.payment.model;

import com.bestsearch.bestsearchservice.order.model.enums.PaymentPreference;
import com.bestsearch.bestsearchservice.payment.dto.PaymentOutputDTO;
import com.bestsearch.bestsearchservice.share.audit.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@SuperBuilder
@NoArgsConstructor
public class Payment extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="order_assign_seq")
    @SequenceGenerator(name = "order_assign_seq", sequenceName = "order_assign_seq" ,allocationSize = 1)
    private Long id;
    private long orderId;
    private String orderRef;
    private BigDecimal totalAmount;
    private Long organizationId;

    private LocalDateTime paymentDate;
    private PaymentPreference paymentPreference;

    public PaymentOutputDTO viewAsPaymentOutputDTO() {
        return PaymentOutputDTO.builder().id(id).orderId(orderId).orderRef(orderRef)
                .totalAmount(totalAmount).organizationId(organizationId).paymentDate(paymentDate)
                .paymentPreference(paymentPreference).build();
    }
}
