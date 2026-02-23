package com.looky.domain.favorite.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.favorite.dto.FavoriteStoreResponse;
import com.looky.domain.favorite.entity.FavoriteStore;
import com.looky.domain.favorite.repository.FavoriteRepository;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.looky.domain.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void addFavorite(User user, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        if (!user.getRole().equals(Role.ROLE_STUDENT)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "학생만 좋아요 등록이 가능합니다.");
        }


        if (favoriteRepository.existsByUserAndStore(user, store)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
        }

        FavoriteStore favoriteStore = FavoriteStore.builder()
                .user(user)
                .store(store)
                .build();

        favoriteRepository.save(favoriteStore);
    }

    @Transactional
    public void removeFavorite(User user, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        favoriteRepository.deleteByUserAndStore(user, store);
    }

    public Long countFavorites(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 상점을 찾을 수 없습니다."));

        return favoriteRepository.countByStore(store);
    }

    public Page<FavoriteStoreResponse> getMyFavorites(User user, Pageable pageable) {
        return favoriteRepository.findByUser(user, pageable)
                .map(favoriteStore -> {
                    Store store = favoriteStore.getStore();
                    Long reviewCountLong = reviewRepository.countByStoreIdAndParentReviewIsNull(store.getId());
                    Integer reviewCount = reviewCountLong != null ? reviewCountLong.intValue() : 0;
                    return FavoriteStoreResponse.from(favoriteStore, reviewCount);
                });
    }
}
