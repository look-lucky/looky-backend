package com.looky.domain.partnership.repository;

import com.looky.domain.partnership.entity.Partnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PartnershipRepository extends JpaRepository<Partnership, Long> {

    @Query("SELECT so FROM Partnership so " +
            "JOIN FETCH so.store s " +
            "JOIN FETCH so.organization o " +
            "JOIN FETCH o.university " +
            "ORDER BY so.id DESC")
    List<Partnership> findAllWithStoreAndOrganization();

    boolean existsByStoreIdAndOrganizationId(Long storeId, Long organizationId);
}
