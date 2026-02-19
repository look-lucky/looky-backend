package com.looky.domain.organization.repository;

import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.OrganizationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByParentIdAndCategory(Long parentId, OrganizationCategory category);
    boolean existsByUniversityId(Long universityId);
    boolean existsByParentId(Long parentId);
    boolean existsByUniversityIdAndName(Long universityId, String name);
    Optional<Organization> findByUniversityIdAndCategory(Long universityId, OrganizationCategory category);
}
