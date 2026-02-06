package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.CouponBenefitType;
import com.looky.domain.coupon.entity.CouponStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UpdateCouponRequest {
    private String title;
    private String description;
    private LocalDateTime issueStartsAt;
    private LocalDateTime issueEndsAt;
    private Integer totalQuantity;
    private Integer limitPerUser;
    private CouponBenefitType benefitType;
    private String benefitValue;
    private Integer minOrderAmount;
    private CouponStatus status;
}
