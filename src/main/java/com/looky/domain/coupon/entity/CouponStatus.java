package com.looky.domain.coupon.entity;

public enum CouponStatus {
    ACTIVE, // 활성
    SOLD_OUT, // 소진됨 (재고 없음)
    EXPIRED, // 만료
    WITHDRAWN_BY_OWNER // 점주 탈퇴로 인한 만료
}
