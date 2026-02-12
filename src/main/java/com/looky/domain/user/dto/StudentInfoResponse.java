package com.looky.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentInfoResponse {
    private String username;
    private String nickname;
    private Long universityId;
    private Long collegeId;
    private Long departmentId;
    private String universityName;
    private String collegeName;
    private String departmentName;
    private Boolean isClubMember;
}
