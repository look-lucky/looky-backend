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
public class IssueCouponResponse {
    private Long studentCouponId;
    private String couponCode;
    private CouponUsageStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String title;

    private CouponBenefitType benefitType;
    private String benefitValue;
    private String storeName;

    public static IssueCouponResponse from(StudentCoupon studentCoupon) {
        return from(studentCoupon, studentCoupon.getCoupon().getStore().getName());
    }

    public static IssueCouponResponse from(StudentCoupon studentCoupon, String storeName) {
        Coupon coupon = studentCoupon.getCoupon();
        return IssueCouponResponse.builder()
                .studentCouponId(studentCoupon.getId())
                .couponCode(studentCoupon.getVerificationCode())
                .status(studentCoupon.getStatus())
                .issuedAt(studentCoupon.getIssuedAt())
                .expiresAt(studentCoupon.getExpiresAt())
                .title(coupon.getTitle())

                .benefitType(coupon.getBenefitType())
                .benefitValue(coupon.getBenefitValue())
                .storeName(storeName)
                .build();
    }
}
