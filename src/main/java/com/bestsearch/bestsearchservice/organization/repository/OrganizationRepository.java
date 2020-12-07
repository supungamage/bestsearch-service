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

    @Query(value = "SELECT  id,name, province,district,city,longitude,latitude,address,geom "
        + "FROM organization "
        + "WHERE ST_DWithin(geom, ST_MakePoint(:longitude,:latitude)::geography, :radius) "
        + "and active = true ; "
        , nativeQuery = true)
    List<Organization> findActiveOrganizationsWithinRadius( double radius, double latitude, double longitude);

    @Query(value = "SELECT * "
        + "FROM organization org "
        + "where active = true "
        + "ORDER BY "
        + "org.geom <-> ST_SetSRID(ST_Point(:longitude, :latitude),4326) "
        + "LIMIT 10 offset :offset ; "
        , nativeQuery = true)
    List<Organization> findOrderedActiveOrganizationsWithinRadius( double latitude, double longitude, int offset);
}
