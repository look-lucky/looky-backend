package com.looky.domain.store.entity;

public enum ClaimRequestStatus {
    PENDING, // 대기
    APPROVED, // 승인 완료
    REJECTED, // 반려
    CANCELED // 취소 (점주가 요청을 보냈다가 스스로 취소)
}
