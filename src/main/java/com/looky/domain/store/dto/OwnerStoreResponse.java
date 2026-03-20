package com.looky.domain.store.dto;

import com.looky.domain.store.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OwnerStoreResponse {
    private Long id;
    private Long userId;
    private String name;
    private String branch;
    private String roadAddress;
    private String jibunAddress;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String representativeName;
    private String introduction;
    private String operatingHours;
    private Boolean needToCheck;
    private List<StoreCategory> storeCategories;
    private List<StoreMood> storeMoods;
    private List<String> imageUrls;
    private List<String> menuBoardImageUrls;
    private Double averageRating;
    private Integer reviewCount;
    private List<LocalDate> holidayDates;
    private Boolean isSuspended;
    private StoreStatus storeStatus;
    private CloverGrade cloverGrade;
    private String profileImageUrl;

    public static OwnerStoreResponse of(Store store, Double averageRating, Integer reviewCount, CloverGrade cloverGrade) {
        return OwnerStoreResponse.builder()
                .id(store.getId())
                .userId(store.getUser() != null ? store.getUser().getId() : null)
                .name(store.getName())
                .branch(store.getBranch())
                .roadAddress(store.getRoadAddress())
                .jibunAddress(store.getJibunAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .phone(store.getStorePhone())
                .representativeName(store.getRepresentativeName())
                .introduction(store.getIntroduction())
                .operatingHours(store.getOperatingHours())
                .needToCheck(store.getNeedToCheck())
                .storeCategories(new ArrayList<>(store.getStoreCategories()))
                .storeMoods(new ArrayList<>(store.getStoreMoods()))
                .imageUrls(store.getImages().stream().map(StoreImage::getImageUrl).collect(Collectors.toList()))
                .menuBoardImageUrls(store.getMenuBoardImages().stream().map(MenuBoardImage::getImageUrl).collect(Collectors.toList()))
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .holidayDates(store.getHolidayDates())
                .isSuspended(store.getIsSuspended())
                .storeStatus(store.getStoreStatus())
                .cloverGrade(cloverGrade)
                .profileImageUrl(store.getProfileImageUrl())
                .build();
    }
}
