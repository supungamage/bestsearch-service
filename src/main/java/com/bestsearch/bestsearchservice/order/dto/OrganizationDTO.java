package com.bestsearch.bestsearchservice.order.dto;

import com.bestsearch.bestsearchservice.organization.dto.OrganizationTypeOutputDTO;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrganizationDTO {
    @JsonView(OrderOutputViews.Public.class)
    private long id;

    @JsonView(OrderOutputViews.Public.class)
    private String name;

    @JsonView(OrderOutputViews.Public.class)
    private String address;

    @JsonView(OrderOutputViews.Public.class)
    private Double longitude;

    @JsonView(OrderOutputViews.Public.class)
    private Double latitude;
}
