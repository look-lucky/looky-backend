package com.looky.domain.store.repository;

import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreReport;
import com.looky.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreReportRepository extends JpaRepository<StoreReport, Long> {
    boolean existsByStoreAndReporter(Store store, User reporter);
}
