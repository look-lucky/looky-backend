package com.looky.domain.store.dto;

import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreCategory;
import com.looky.domain.store.entity.StoreImage;
import com.looky.domain.store.entity.StoreMood;
import com.looky.domain.store.entity.CloverGrade;

import lombok.Builder;
import lombok.Getter;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class StoreResponse {
    private Long id;
    private Long userId; // Owner ID
    private String name;
    private String branch; // 지점명
    private String roadAddress; // 도로명 주소
    private String jibunAddress; // 지번 주소
    private Double latitude;
    private Double longitude;
    private String phone;
    private String representativeName;

    private String introduction;
    private String operatingHours;
    private Boolean needToCheck;
    private List<StoreCategory> storeCategories;
    private List<StoreMood> storeMoods;
    private List<String> imageUrls; // 0번 째 값이 썸네일
    private Double averageRating;
    private Integer reviewCount;
    private List<LocalDate> holidayDates;
    private Boolean isSuspended;
    private List<String> myPartnerships; // 내가 속한 조직 중 제휴 맺은 조직 이름 목록
    private Boolean hasCoupon; // 쿠폰 보유 여부
    private CloverGrade cloverGrade; // 클로버 등급

    public static StoreResponse of(Store store, Double averageRating, Integer reviewCount, List<String> myPartnerships, Boolean hasCoupon, CloverGrade cloverGrade) {
        return StoreResponse.builder()
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
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .holidayDates(store.getHolidayDates())
                .isSuspended(store.getIsSuspended())
                .myPartnerships(myPartnerships)
                .hasCoupon(hasCoupon)
                .cloverGrade(cloverGrade)
                .build();
    }

    public static StoreResponse from(Store store) {
        return of(store, 0.0, 0, null, false, store.getCloverGrade());
    }
}
