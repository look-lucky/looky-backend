package com.looky.domain.organization.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrganizationCategory {
    COLLEGE, // 단과대학
    DEPARTMENT, // 학과
    STUDENT_COUNCIL, // 학생회
}
