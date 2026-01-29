package com.looky.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WithdrawalReason {
    UNUSED("서비스를 잘 이용하지 않아요"),
    INSUFFICIENT_BENEFITS("매력적인 혜택이 부족해요"),
    INCONVENIENT("앱/서비스 이용이 너무 불편해요"),
    TOO_MANY_ADS("잦은 알림과 광고가 부담스러워요"),
    NOT_NEEDED("더 이상 필요한 상품/서비스가 없어요"),
    OTHER("기타");

    private final String description;
}
