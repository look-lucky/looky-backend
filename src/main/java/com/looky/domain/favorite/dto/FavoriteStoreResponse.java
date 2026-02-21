package com.looky.domain.favorite.dto;

import com.looky.domain.favorite.entity.FavoriteStore;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteStoreResponse {

    private Long storeId;
    private String name;
    private String roadAddress;
    private String jibunAddress;
    private List<StoreCategory> storeCategories;
    private String imageUrl; // 대표이미지
    private Double averageRating; // 별점 평균
    private Integer reviewCount; // 리뷰 수
    private LocalDateTime createdAt; // 찜한 시간

    public static FavoriteStoreResponse from(FavoriteStore favoriteStore, Integer reviewCount) {
        Store store = favoriteStore.getStore();
        return FavoriteStoreResponse.builder()
                .storeId(store.getId())
                .name(store.getName())
                .roadAddress(store.getRoadAddress())
                .jibunAddress(store.getJibunAddress())
                .storeCategories(new ArrayList<>(store.getStoreCategories()))
                .averageRating(store.getAverageRating())
                .reviewCount(reviewCount)
                .createdAt(favoriteStore.getCreatedAt())
                .build();
    }
}
