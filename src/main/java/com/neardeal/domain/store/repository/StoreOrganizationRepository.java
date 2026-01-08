package com.neardeal.domain.store.repository;

import com.neardeal.domain.store.entity.StoreOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreOrganizationRepository extends JpaRepository<StoreOrganization, Long> {

    @Query("SELECT so FROM StoreOrganization so " +
            "JOIN FETCH so.store s " +
            "JOIN FETCH so.organization o " +
            "JOIN FETCH o.university " +
            "ORDER BY so.id DESC")
    List<StoreOrganization> findAllWithStoreAndOrganization();
}
