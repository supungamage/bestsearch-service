package com.bestsearch.bestsearchservice.organization.service;

import com.bestsearch.bestsearchservice.organization.dto.OrganizationTypeInputDTO;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationTypeOutputDTO;

import java.util.List;


public interface OrganizationTypeService { 
	
	OrganizationTypeOutputDTO addOrganizationType(OrganizationTypeInputDTO organizationTypeInputDTO);
	
	List<OrganizationTypeOutputDTO> getOrganizationTypes();
	
	OrganizationTypeOutputDTO getOrganizationTypeById(long id);
	
	OrganizationTypeOutputDTO updateOrganizationType(long id, OrganizationTypeInputDTO organizationTypeInputDTO);
}
