package com.looky.domain.user.dto;

import com.looky.domain.user.entity.Gender;
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
    private String username; // 아이디 (이메일X)
    private String password;
    private String nickname;
    private Gender gender;
    private LocalDate birthDate;
    
    private Long universityId;
    private Long collegeId; 
    private Long departmentId; 
}
