package com.looky.domain.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilSignupRequest {
    private String username; // 아이디 (이메일X)
    private String password;
    private String name;
    private Long universityId;
}
