package com.bestsearch.bestsearchservice.order.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderAndPeriodDTO {
    @JsonView(OrderOutputViews.Public.class)
    private String period;

    @JsonView(OrderOutputViews.Public.class)
    private List<OrderOutputDTO> items;
}
