package com.looky.domain.storeclaim.repository;

import com.looky.domain.storeclaim.entity.StoreClaim;
import com.looky.domain.storeclaim.entity.StoreClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreClaimRepository extends JpaRepository<StoreClaim, Long> {
    Page<StoreClaim> findByStatus(StoreClaimStatus status, Pageable pageable);
    boolean existsByStoreIdAndStatus(Long storeId, StoreClaimStatus status);
    List<StoreClaim> findByUserId(Long userId);
}
