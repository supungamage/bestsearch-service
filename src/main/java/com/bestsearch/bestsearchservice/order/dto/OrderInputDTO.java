package com.bestsearch.bestsearchservice.order.dto;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderInputDTO {

    private String orderRef;
    private long userId;
    private OrderType orderType;
    private Status status;
    private long organizationTypeId;
    private Double longitude;
    private Double latitude;
    private long organizationId;
}
