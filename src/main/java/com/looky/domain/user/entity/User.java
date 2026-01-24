package com.looky.domain.user.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.security.oauth.OAuth2UserInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 일반 로그인 & 소셜 로그인 공통 식별자
    // 일반 로그인 -> 아이디 형식 / 소셜 로그인 -> provider_id
    @Column(nullable = false, unique = true)
    private String username;

    // 일반 로그인용 (소셜 로그인 사용자는 null)
    private String password;

    // 사용자 이름
    @Column
    private String name;

    // 사용자 전화번호
    private String phoneNumber;

    // 사용자 성별
    private Gender gender;

    // 사용자 나이
    private LocalDate birthDate;

    // 권한 (USER, ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 소셜 로그인 정보
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    // 소셜 식별 값 (예: 카카오의 회원번호)
    private String socialId;

    @Builder
    public User(String username, String password, String name, String phoneNumber, Gender gender, LocalDate birthDate, Role role, SocialType socialType, String socialId) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.birthDate = birthDate;
        this.role = role;
        this.socialType = socialType;
        this.socialId = socialId;
    }

    // 소셜 가입 후 부족한 정보 완성 -> ROLE_GUEST에서 승격
    public void completeInsufficientInfo(Role role, String phoneNumber) {
        this.role = role;
        this.phoneNumber = phoneNumber;
    }

    // 이미 가입된 소셜 식벼자일 때 나머지 정보 최신화
    public User updateSocialInfo(OAuth2UserInfo userInfo) {
        if (userInfo.getName() != null && !userInfo.getName().isEmpty()) {
            this.name = userInfo.getName();
        }
        return this;
    }

    public void setUserId(Long userId) {
        this.id = userId;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }
}
