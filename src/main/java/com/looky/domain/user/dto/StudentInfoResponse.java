package com.looky.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentInfoResponse {
    private Long universityId;
    private Long collegeId;
    private Long departmentId;
    private Boolean isClubMember;
}
