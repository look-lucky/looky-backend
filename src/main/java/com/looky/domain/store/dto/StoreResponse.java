package com.looky.domain.store.dto;

import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreImage;
import com.looky.domain.store.entity.StoreMood;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class StoreResponse {
    private Long id;
    private Long userId; // Owner ID
    private String name;
    private String roadAddress; // 도로명 주소
    private String jibunAddress; // 지번 주소
    private Double latitude;
    private Double longitude;
    private String phone;
    private String introduction;
    private String operatingHours;
    private Boolean needToCheck;
    private List<StoreCategory> storeCategories;
    private List<StoreMood> storeMoods;
    private List<String> imageUrls; // 0번 째 값이 썸네일
    private Double averageRating;
    private Integer reviewCount;

    public static StoreResponse of(Store store, Double averageRating, Integer reviewCount) {
        return StoreResponse.builder()
                .id(store.getId())
                .userId(store.getUser() != null ? store.getUser().getId() : null)
                .name(store.getName())
                .roadAddress(store.getRoadAddress())
                .jibunAddress(store.getJibunAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .phone(store.getStorePhone())
                .introduction(store.getIntroduction())
                .operatingHours(store.getOperatingHours())
                .needToCheck(store.getNeedToCheck())
                .storeCategories(new ArrayList<>(store.getStoreCategories()))
                .storeMoods(new ArrayList<>(store.getStoreMoods()))
                .imageUrls(store.getImages().stream().map(StoreImage::getImageUrl).collect(Collectors.toList()))
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .build();
    }

    public static StoreResponse from(Store store) {
        return of(store, 0.0, 0);
    }
}
