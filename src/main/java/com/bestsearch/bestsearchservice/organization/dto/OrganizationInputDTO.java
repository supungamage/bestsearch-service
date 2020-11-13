package com.bestsearch.bestsearchservice.organization.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationInputDTO {
	private Long id;
	private String name;
	private long organizationTypeId;
	private String province;
	private String district;
	private String city;
	private Double longitude;
	private Double latitude;
}
