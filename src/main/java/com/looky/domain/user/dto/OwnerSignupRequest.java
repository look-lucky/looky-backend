package com.looky.domain.user.dto;

import com.looky.domain.user.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerSignupRequest {
    private String username; // 아이디 (이메일X)
    private String password;
    private String name;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private LocalDate birthDate;

    private List<StoreCreateRequest> storeList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreCreateRequest {
        private String name;
        private String address;
        private Long partnerUniversityId;
        private List<Long> partnerOrganizationIds;
        private String businessRegistrationNumber;
    }
}
