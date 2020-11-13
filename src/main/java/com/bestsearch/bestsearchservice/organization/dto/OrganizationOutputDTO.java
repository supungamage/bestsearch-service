package com.bestsearch.bestsearchservice.organization.dto;

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
public class OrganizationOutputDTO {
	private long id;
	private String name;
	private String address;
	private String province;
	private String district;
	private String city;
	private Double longitude;
	private Double latitude;
	private OrganizationTypeOutputDTO type;
	private boolean active;
}
