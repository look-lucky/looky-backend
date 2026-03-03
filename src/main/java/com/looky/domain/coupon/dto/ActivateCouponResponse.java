package com.looky.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ActivateCouponResponse {
    private String verificationCode; // 쿠폰 사용 코드
    private LocalDateTime activationExpiresAt; // 쿠폰 사용 코드의 만료 시간
}
