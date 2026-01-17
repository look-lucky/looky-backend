package com.neardeal.domain.storenews.repository;

import com.neardeal.domain.storenews.entity.StoreNewsLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreNewsLikeRepository extends JpaRepository<StoreNewsLike, Long> {
    Optional<StoreNewsLike> findByStoreNewsIdAndUserId(Long storeNewsId, Long userId);

    boolean existsByStoreNewsIdAndUserId(Long storeNewsId, Long userId);
}
