package com.looky.domain.review.entity;

public enum ReportReason {
    SPAM, // 스팸
    INAPPROPRIATE_CONTENT, // 부적절한 내용 (욕설, 불법 정보, 혐오 표현...)
    IRRELEVANT, // 가게 또는 상품과 무관한 내용
    OTHER // 기타 -> 상세 사유 필수로 입력하게 해야 함
}
