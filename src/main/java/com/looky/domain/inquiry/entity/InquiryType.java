package com.looky.domain.inquiry.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryType {
    COUPON_BENEFIT("쿠폰·혜택 사용"),
    MAP_LOCATION("지도·위치 문의"),
    STORE_INFO_ERROR("매장 정보 오류"),
    EVENT_PARTICIPATION("이벤트 참여"),
    ALERT_ACCOUNT("알림·계정"),
    PROPOSAL_OTHER("행운 제안·기타");

    private final String description;
}
