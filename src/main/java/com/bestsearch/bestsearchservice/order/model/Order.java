package com.bestsearch.bestsearchservice.order.model;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.share.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "orders")
@Getter
@SuperBuilder
@NoArgsConstructor
public class Order extends Auditable<String> {
    @Id
    private long id;

    private String orderRef;
    private long userId;

    @Enumerated(EnumType.ORDINAL)
    private OrderType orderType;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    private long organizationTypeId;

    @NonNull
    private Double longitude;

    @NonNull
    private Double latitude;

    private long organizationId;

    private LocalDateTime orderedAt;

    private String userComment;

    @ElementCollection
    private List<String> images;

    @JsonIgnore
    public OrderOutputDTO viewAsOrderOutputDTO() {
        return OrderOutputDTO.builder().id(id).orderRef(orderRef).userId(userId).orderType(orderType)
                .status(status).latitude(latitude).longitude(longitude)
                .organizationTypeId(organizationTypeId)
                .organizationId(organizationId)
                .orderedAt(orderedAt)
                .period(ChronoUnit.HOURS.between(orderedAt, LocalDateTime.now()) < 24
                        ? ChronoUnit.HOURS.between(orderedAt, LocalDateTime.now())
                        : -1)
                .images(images)
                .userComment(userComment)
                .build();
    }
}
