package com.looky.domain.review.entity;

public enum ReviewStatus {

    PUBLISHED, // 게시됨
    REPORTED,  // 신고 접수됨
    BANNED,    // 관리자에 의해 차단됨
    VERIFIED   // 신고되었으나 관리자가 문제 없음 판단

}
