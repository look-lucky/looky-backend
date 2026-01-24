package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.StudentCoupon;
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

    public static IssueCouponResponse from(StudentCoupon studentCoupon) {
        return IssueCouponResponse.builder()
                .studentCouponId(studentCoupon.getId())
                .couponCode(studentCoupon.getVerificationCode())
                .status(studentCoupon.getStatus())
                .issuedAt(studentCoupon.getIssuedAt())
                .expiresAt(studentCoupon.getExpiresAt())
                .build();
    }
}
