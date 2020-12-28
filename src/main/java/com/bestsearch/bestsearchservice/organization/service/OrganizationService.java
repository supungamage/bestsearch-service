package com.bestsearch.bestsearchservice.organization.service;

import com.bestsearch.bestsearchservice.organization.dto.OrganizationInputDTO;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;

import com.bestsearch.bestsearchservice.organization.dto.OrganizationsListOutputDTO;
import java.util.List;

public interface OrganizationService {

    OrganizationOutputDTO addOrganization(OrganizationInputDTO organizationInputDTO);

    List<OrganizationsListOutputDTO> getActiveOrganizationsByType(long organizationTypeId);

    OrganizationOutputDTO getOrganizationById(long id);

    OrganizationOutputDTO updateOrganization(long id, OrganizationInputDTO organizationInputDTO);

    List<OrganizationOutputDTO> getActiveOrganizationsWithinRadius(double radius, double latitude, double longitude);

    List<OrganizationOutputDTO> getOrderedActiveOrganizationsWithinRadius(double latitude, double longitude, int offset);

    List<OrganizationOutputDTO> getOrganizationByIds(List<Long> ids);
}
