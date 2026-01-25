package com.looky.domain.store.repository;

import com.looky.domain.store.entity.StoreClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreClaimRepository extends JpaRepository<StoreClaim, Long> {
}
