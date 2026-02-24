package com.looky.domain.store.dto;

import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreImage;
import com.looky.domain.partnership.dto.PartnershipInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
public class StoreMapResponse {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String imageUrl; // 썸네일 (첫 번째 이미지)
    private Double averageRating;
    private Integer reviewCount;
    private List<StoreCategory> storeCategories;
    private String operatingHours;
    private List<PartnershipInfo> myPartnerships;
    private Boolean hasCoupon;
    private Long favoriteCount;

    public static StoreMapResponse of(Store store, Double averageRating, Integer reviewCount, List<PartnershipInfo> myPartnerships, Boolean hasCoupon, Long favoriteCount) {
        // 이미지 중 첫 번째 이미지를 썸네일로 사용
        String thumbnailUrl = store.getImages().stream()
                .sorted(Comparator.comparingInt(StoreImage::getOrderIndex))
                .map(StoreImage::getImageUrl)
                .findFirst()
                .orElse(null);

        return StoreMapResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .imageUrl(thumbnailUrl)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .storeCategories(new ArrayList<>(store.getStoreCategories()))
                .operatingHours(store.getOperatingHours())
                .myPartnerships(myPartnerships)
                .hasCoupon(hasCoupon)
                .favoriteCount(favoriteCount != null ? favoriteCount : 0L)
                .build();
    }
}
