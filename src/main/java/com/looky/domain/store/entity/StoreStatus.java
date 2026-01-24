package com.looky.domain.store.entity;

public enum StoreStatus {
    UNCLAIMED, // 주인 없음 (앱에 노출되지만 사장님 기능 잠김)
    ACTIVE, // 운영 중 (사장님이 점유 요청하고 관리자가 승인한 상태)
    BANNED // 정지
}
