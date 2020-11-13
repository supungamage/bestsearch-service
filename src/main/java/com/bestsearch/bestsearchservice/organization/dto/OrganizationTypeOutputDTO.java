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
public class OrganizationTypeOutputDTO { 
	private long id;
	private String type;
	private boolean active;
}
