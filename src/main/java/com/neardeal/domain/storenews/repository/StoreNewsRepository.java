package com.neardeal.domain.storenews.repository;

import com.neardeal.domain.storenews.entity.StoreNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreNewsRepository extends JpaRepository<StoreNews, Long> {
    Page<StoreNews> findByStoreId(Long storeId, Pageable pageable);
}
