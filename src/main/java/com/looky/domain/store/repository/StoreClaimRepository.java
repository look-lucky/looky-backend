package com.looky.domain.store.repository;

import com.looky.domain.store.entity.StoreClaim;
import com.looky.domain.store.entity.StoreClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreClaimRepository extends JpaRepository<StoreClaim, Long> {
    Page<StoreClaim> findByStatus(StoreClaimStatus status, Pageable pageable);
    boolean existsByStoreIdAndStatus(Long storeId, StoreClaimStatus status);
}
