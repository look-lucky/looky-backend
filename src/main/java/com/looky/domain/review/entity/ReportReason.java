package com.looky.domain.review.entity;

public enum ReportReason {
    MALICIOUS_SLANDER,      // 근거 없이 악의적인 내용
    INAPPROPRIATE_CONTENT,  // 음란성 또는 욕설 등 부적절한 내용
    RIGHTS_VIOLATION,       // 명예훼손 및 저작권 침해
    PRIVACY_INFRINGEMENT,   // 초상권 침해 또는 개인정보 노출
    COMMERCIAL_PROMOTION,   // 서비스나 메뉴 등 대가를 목적으로 작성된 리뷰
    FRAUDULENT_REVIEW,      // 리뷰 작성 대행업체를 통해 게재된 허위 리뷰
    OTHER                   // 기타 (상세 사유 필수 입력)
}