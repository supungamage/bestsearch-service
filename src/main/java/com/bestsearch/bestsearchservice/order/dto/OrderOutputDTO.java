package com.bestsearch.bestsearchservice.order.dto;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrderOutputDTO {
    private long id;
    private String orderRef;
    private long userId;
    private OrderType orderType;
    private Status status;
    private long organizationTypeId;
    private Double longitude;
    private Double latitude;
    private long organizationId;
    private LocalDateTime orderedAt;
    private long period;

    @JsonIgnore
    public LocalDate getOrderDate() {
        return orderedAt.toLocalDate();
    }
}
