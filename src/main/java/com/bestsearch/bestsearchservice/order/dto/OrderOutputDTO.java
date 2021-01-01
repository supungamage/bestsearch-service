package com.bestsearch.bestsearchservice.order.dto;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
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
    @JsonView(OrderOutputViews.Public.class)
    private long id;

    @JsonView(OrderOutputViews.Public.class)
    private String orderRef;

    @JsonView(OrderOutputViews.Public.class)
    private long userId;

    @JsonView(OrderOutputViews.Public.class)
    private OrderType orderType;

    @JsonView(OrderOutputViews.Public.class)
    private Status status;

    @JsonView(OrderOutputViews.Public.class)
    private long organizationTypeId;

    @JsonView(OrderOutputViews.Public.class)
    private Double longitude;

    @JsonView(OrderOutputViews.Public.class)
    private Double latitude;

    @JsonView(OrderOutputViews.Public.class)
    private long organizationId;

    @JsonView(OrderOutputViews.Public.class)
    private OrganizationDTO organizationDTO;

    @JsonView(OrderOutputViews.Internal.class)
    private LocalDateTime orderedAt;

    @JsonView(OrderOutputViews.Public.class)
    private long period;

    @JsonView(OrderOutputViews.Public.class)
    private String userComment;

    @JsonIgnore
    public LocalDate getOrderDate() {
        return orderedAt.toLocalDate();
    }
}
