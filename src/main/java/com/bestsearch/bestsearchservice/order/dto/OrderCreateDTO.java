package com.bestsearch.bestsearchservice.order.dto;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrderCreateDTO {
    private Long userId;
    private OrderType orderType;
    private Long organizationTypeId;
    private Double longitude;
    private Double latitude;
    private Long organizationId;
    private String userComment;
    private List<String> images;
}
