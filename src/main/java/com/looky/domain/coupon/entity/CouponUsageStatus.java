package com.looky.domain.coupon.entity;

public enum CouponUsageStatus {
    UNUSED, // 사용 전
    ACTIVATED, // 쿠폰 활성화 (사용 버튼 누름, 코드 생성됨)
    USED, // 사용 완료
    EXPIRED // 만료
}
