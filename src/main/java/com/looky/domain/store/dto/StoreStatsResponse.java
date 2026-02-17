package com.looky.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreStatsResponse {
    private Long totalRegulars; // 총 단골 수 (찜 수)
    private Long totalIssuedCoupons; // 총 발급 쿠폰
    private Long totalUsedCoupons; // 총 사용 쿠폰
    private Long totalReviews; // 총 리뷰 수
    private Long favoriteIncreaseCount; // 이번 주 찜 증가 수 (지난 주 대비)
}
