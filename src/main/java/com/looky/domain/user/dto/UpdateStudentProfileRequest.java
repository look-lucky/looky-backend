package com.looky.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateStudentProfileRequest {
    private String nickname;
    private Long collegeId;
    private Long departmentId;
    private Boolean isClubMember;
}
