package com.bestsearch.bestsearchservice.organization.model;

import com.bestsearch.bestsearchservice.organization.dto.OrganizationTypeOutputDTO;
import com.bestsearch.bestsearchservice.share.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "organization_type")
@Getter
@SuperBuilder
@NoArgsConstructor
public class OrganizationType extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="org_type_seq")
    @SequenceGenerator(name = "org_type_seq", sequenceName = "org_type_seq" ,allocationSize = 1)
	private long id;
	
	private String type;

	@Builder.Default
	private boolean active = true;
	
	@OneToMany(mappedBy = "organizationType", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<Organization> organizations;
	
	@JsonIgnore
	public OrganizationTypeOutputDTO viewAsOrganizationTypeOutputDTO() {
		return OrganizationTypeOutputDTO.builder().id(id).type(type).active(active).build();
	}
}
