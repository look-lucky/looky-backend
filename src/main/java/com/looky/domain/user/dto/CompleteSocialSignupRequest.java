package com.looky.domain.user.dto;

import com.looky.domain.user.entity.Gender;
import com.looky.domain.user.entity.Role;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class CompleteSocialSignupRequest {
    private Role role;

    // 공통
    private Gender gender;
    private LocalDate birthDate;

    // 학생 
    private String nickname;
    private Long universityId;
    private Long collegeId;
    private Long departmentId;
    private Boolean isClubMember;

    // 점주
    private String name;
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;
}
