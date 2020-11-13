package com.bestsearch.bestsearchservice.organization.repository;

import com.bestsearch.bestsearchservice.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<List<Organization>> findByOrganizationType(long organizationTypeId);

    @Query(value = "SELECT * FROM organization o " +
            "WHERE o.organization_type = :organizationTypeId AND o.active = :isActive" ,nativeQuery = true)
    Optional<List<Organization>> findActiveOrganizationByType(long organizationTypeId, boolean isActive);
}
