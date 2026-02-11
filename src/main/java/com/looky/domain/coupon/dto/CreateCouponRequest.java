package com.looky.domain.coupon.dto;

import com.looky.domain.coupon.entity.CouponBenefitType;
import com.looky.domain.coupon.entity.CouponStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCouponRequest {

    @NotBlank(message = "쿠폰명은 필수입니다.")
    private String title;



    private LocalDateTime issueStartsAt;
    private LocalDateTime issueEndsAt;

    @NotNull(message = "총 발행 수량은 필수입니다.")
    private Integer totalQuantity;

    @NotNull(message = "인당 발행 한도는 필수입니다.")
    private Integer limitPerUser;

    @NotNull(message = "혜택 타입은 필수입니다.")
    private CouponBenefitType benefitType;

    private String benefitValue;

    private Integer minOrderAmount; 

    private CouponStatus status; 
}
