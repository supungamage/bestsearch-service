package com.bestsearch.bestsearchservice.organization.model;

import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.share.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@Table(name = "organization")
@Getter
@SuperBuilder
@NoArgsConstructor
public class Organization extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="org_seq")
    @SequenceGenerator(name = "org_seq", sequenceName = "org_seq" ,allocationSize = 1)
	private long id;
	
	@NonNull
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organizationType")
	@JsonIgnore
	private OrganizationType organizationType;
	
	private String province;
	private String district;
	private String city;
	
	@NonNull
	private Double longitude;
	
	@NonNull
	private Double latitude;

	@Builder.Default
	private boolean active = true;

	private String address;

	@JsonIgnore
	public OrganizationOutputDTO viewAsOrganizationOutputDTO() {
		return OrganizationOutputDTO.builder().id(id).name(name).city(city).district(district)
				.province(province).active(active).latitude(latitude).longitude(longitude)
				.address(address).type(organizationType.viewAsOrganizationTypeOutputDTO())
				.build();
	}
}
