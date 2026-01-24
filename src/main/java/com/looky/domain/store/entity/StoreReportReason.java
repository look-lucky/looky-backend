package com.looky.domain.store.entity;

public enum StoreReportReason {
    BENEFIT_REFUSAL, // 혜택 제공 거부
    BENEFIT_MISMATCH, // 혜택 내용 상이
    EVENT_NOT_HELD, // 실시간 이벤트 미이행
    CLOSED_OR_MOVED, // 폐업 또는 이전
    INFO_ERROR, // 가격/메뉴 정보 오류
    LOCATION_MISMATCH, // 위치 정보 불일치
    ETC // 기타
}
