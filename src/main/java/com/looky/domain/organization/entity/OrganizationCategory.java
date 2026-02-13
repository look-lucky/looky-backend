package com.looky.domain.organization.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrganizationCategory {
    COLLEGE, // 단과대학
    DEPARTMENT, // 학과
    UNIVERSITY_COUNCIL, // 총학생회
    CLUB_ASSOCIATION, // 총동아리연합회
}
