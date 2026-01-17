package com.neardeal.domain.storenews.repository;

import com.neardeal.domain.storenews.entity.StoreNewsComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreNewsCommentRepository extends JpaRepository<StoreNewsComment, Long> {
    Page<StoreNewsComment> findByStoreNewsId(Long storeNewsId, Pageable pageable);
}
