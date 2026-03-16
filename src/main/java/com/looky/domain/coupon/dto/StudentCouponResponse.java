package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.Coupon;
import com.looky.domain.coupon.entity.CouponBenefitType;
import com.looky.domain.coupon.entity.CouponStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudentCouponResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private String title;
    private LocalDateTime issueStartsAt;
    private LocalDateTime issueEndsAt;
    private Integer validDays;
    private Integer totalQuantity;
    private Integer limitPerUser;
    private CouponStatus status;
    private CouponBenefitType benefitType;
    private String benefitValue;
    private Integer minOrderAmount;
    private Integer downloadCount;
    private Boolean isDownloaded;

    public static StudentCouponResponse from(Coupon coupon, boolean isDownloaded) {
        return StudentCouponResponse.builder()
                .id(coupon.getId())
                .storeId(coupon.getStore().getId())
                .storeName(coupon.getStore().getName())
                .title(coupon.getTitle())
                .issueStartsAt(coupon.getIssueStartsAt())
                .issueEndsAt(coupon.getIssueEndsAt())
                .validDays(coupon.getValidDays())
                .totalQuantity(coupon.getTotalQuantity())
                .limitPerUser(coupon.getLimitPerUser())
                .status(coupon.getStatus())
                .benefitType(coupon.getBenefitType())
                .benefitValue(coupon.getBenefitValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .downloadCount(coupon.getDownloadCount())
                .isDownloaded(isDownloaded)
                .build();
    }
}
