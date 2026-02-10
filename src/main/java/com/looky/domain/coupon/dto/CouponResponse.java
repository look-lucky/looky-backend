package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.Coupon;
import com.looky.domain.coupon.entity.CouponBenefitType;
import com.looky.domain.coupon.entity.CouponStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
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
    private Integer downloadCount; // 현재 다운로드된 수량
    private Long usedCount; // 사용 완료된 수량 (점주용)
    private Boolean isDownloaded; // 다운로드 여부 (학생용)

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
                .downloadCount(coupon.getDownloadCount())
                .isDownloaded(false) // Default to false
                .build();
    }
}
