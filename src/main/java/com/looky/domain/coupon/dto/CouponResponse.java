package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.Coupon;
import com.looky.domain.coupon.entity.CouponBenefitType;
import com.looky.domain.coupon.entity.CouponStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponse {
    private Long id;
    private Long storeId;
    private String title;
    private String description;
    private LocalDateTime issueStartsAt;
    private LocalDateTime issueEndsAt;
    private Integer totalQuantity;
    private Integer limitPerUser;
    private CouponStatus status;
    private CouponBenefitType benefitType;
    private String benefitValue;
    private Integer minOrderAmount;
    private Boolean isIssued; // 발급 여부 (학생용)

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .storeId(coupon.getStore().getId())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .issueStartsAt(coupon.getIssueStartsAt())
                .issueEndsAt(coupon.getIssueEndsAt())
                .totalQuantity(coupon.getTotalQuantity())
                .limitPerUser(coupon.getLimitPerUser())
                .status(coupon.getStatus())
                .benefitType(coupon.getBenefitType())
                .benefitValue(coupon.getBenefitValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .isIssued(false) // Default to false
                .build();
    }

    public void setIsIssued(boolean isIssued) {
        this.isIssued = isIssued;
    }
}
