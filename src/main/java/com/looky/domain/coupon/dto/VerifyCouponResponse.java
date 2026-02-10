package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.Coupon;
import com.looky.domain.coupon.entity.StudentCoupon;
import com.looky.domain.coupon.entity.CouponBenefitType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VerifyCouponResponse {
    private Long studentCouponId;
    private String studentNickname;
    private String couponTitle;
    private String description;
    private CouponBenefitType benefitType;
    private String benefitValue;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private Boolean isExpired;

    public static VerifyCouponResponse from(StudentCoupon studentCoupon, String studentNickname) {
        Coupon coupon = studentCoupon.getCoupon();
        return VerifyCouponResponse.builder()
                .studentCouponId(studentCoupon.getId())
                .studentNickname(studentNickname)
                .couponTitle(coupon.getTitle())
                .description(coupon.getDescription())
                .benefitType(coupon.getBenefitType())
                .benefitValue(coupon.getBenefitValue())
                .issuedAt(studentCoupon.getIssuedAt())
                .expiresAt(studentCoupon.getExpiresAt())
                .isExpired(studentCoupon.getExpiresAt().isBefore(LocalDateTime.now()))
                .build();
    }
}
