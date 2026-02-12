package com.looky.domain.user.dto;

import com.looky.domain.user.entity.Gender;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSignupRequest {
    @Pattern(regexp = "^[a-zA-Z0-9]{6,16}$", message = "아이디는 영문, 숫자 포함 6자 이상 16자 이하여야 합니다.")
    private String username; // 아이디 (이메일X)
    private String password;
    private String email; 
    private String nickname;
    private Gender gender;
    private LocalDate birthDate;
    
    private Long universityId;
    private Long collegeId; 
    private Long departmentId; 
    private Boolean isClubMember;
}
