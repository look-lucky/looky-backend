package com.looky.domain.user.dto;

import com.looky.domain.user.dto.OwnerSignupRequest.StoreCreateRequest;
import com.looky.domain.user.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompleteSocialSignupRequest {
    private Role role;

    // 공통
    private String name;
    private String email;
    private String phoneNumber;

    // 학생 
    private String nickname;
    private Long universityId;
    private Long collegeId;
    private Long departmentId;

    // 점주 
    private List<StoreCreateRequest> storeList;
}
