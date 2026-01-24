package com.looky.domain.coupon.entity;

public enum CouponStatus {
    DRAFT, // 임시저장
    SCHEDULED, // 예약
    ACTIVE, // 활성
    STOPPED, // 일시중지
    EXPIRED // 만료
}
