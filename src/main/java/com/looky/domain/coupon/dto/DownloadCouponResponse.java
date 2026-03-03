package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.Coupon;
import com.looky.domain.coupon.entity.StudentCoupon;
import com.looky.domain.coupon.entity.CouponBenefitType;
import com.looky.domain.coupon.entity.CouponUsageStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class DownloadCouponResponse {
    private Long studentCouponId;
    private String couponCode;
    private CouponUsageStatus status;
    private LocalDateTime downloadedAt;
    private LocalDateTime expiresAt;
    private String title;

    private CouponBenefitType benefitType;
    private String benefitValue;
    private Integer minOrderAmount;
    private String storeName;
    private LocalDateTime activationExpiresAt;

    public static DownloadCouponResponse from(StudentCoupon studentCoupon) {
        return from(studentCoupon, studentCoupon.getCoupon().getStore().getName());
    }

    public static DownloadCouponResponse from(StudentCoupon studentCoupon, String storeName) {
        Coupon coupon = studentCoupon.getCoupon();

        // 쿠폰 사용 코드 만료 시간 계산
        LocalDateTime activationExpiresAt = null;
        if (studentCoupon.getStatus() == CouponUsageStatus.ACTIVATED && studentCoupon.getActivatedAt() != null) {
            activationExpiresAt = studentCoupon.getActivatedAt().plusMinutes(5);
        }

        return DownloadCouponResponse.builder()
                .studentCouponId(studentCoupon.getId())
                .couponCode(studentCoupon.getVerificationCode())
                .status(studentCoupon.getStatus())
                .downloadedAt(studentCoupon.getDownloadedAt())
                .expiresAt(studentCoupon.getExpiresAt())
                .title(coupon.getTitle())

                .benefitType(coupon.getBenefitType())
                .benefitValue(coupon.getBenefitValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .storeName(storeName)
                .activationExpiresAt(activationExpiresAt)
                .build();
    }
}
