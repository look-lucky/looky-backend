package com.looky.domain.store.dto;

import com.looky.domain.partnership.dto.PartnershipInfo;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreImage;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
public class StudentStoreMapResponse {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private Double averageRating;
    private Integer reviewCount;
    private List<StoreCategory> storeCategories;
    private String operatingHours;
    private List<PartnershipInfo> myPartnerships; // 항상 존재 (빈 리스트 가능, null 아님)
    private Boolean hasCoupon;                     // 항상 존재
    private Long favoriteCount;

    public static StudentStoreMapResponse of(Store store, Double averageRating, Integer reviewCount, List<PartnershipInfo> myPartnerships, Boolean hasCoupon, Long favoriteCount) {
        String thumbnailUrl = store.getImages().stream()
                .sorted(Comparator.comparingInt(StoreImage::getOrderIndex))
                .map(StoreImage::getImageUrl)
                .findFirst()
                .orElse(null);

        return StudentStoreMapResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .imageUrl(thumbnailUrl)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .storeCategories(new ArrayList<>(store.getStoreCategories()))
                .operatingHours(store.getOperatingHours())
                .myPartnerships(myPartnerships != null ? myPartnerships : List.of())
                .hasCoupon(hasCoupon)
                .favoriteCount(favoriteCount != null ? favoriteCount : 0L)
                .build();
    }
}
