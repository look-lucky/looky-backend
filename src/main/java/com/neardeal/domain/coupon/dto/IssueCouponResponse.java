package com.neardeal.domain.coupon.dto;

import com.neardeal.domain.coupon.entity.CustomerCoupon;
import com.neardeal.domain.coupon.entity.CouponUsageStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class IssueCouponResponse {
    private Long customerCouponId;
    private String couponCode;
    private CouponUsageStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    public static IssueCouponResponse from(CustomerCoupon customerCoupon) {
        return IssueCouponResponse.builder()
                .customerCouponId(customerCoupon.getId())
                .couponCode(customerCoupon.getVerificationCode())
                .status(customerCoupon.getStatus())
                .issuedAt(customerCoupon.getIssuedAt())
                .expiresAt(customerCoupon.getExpiresAt())
                .build();
    }
}
