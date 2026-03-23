package com.looky.domain.user.dto;

import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.SocialType;
import com.looky.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String username;
    private Role role;
    private SocialType socialType;

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .socialType(user.getSocialType())
                .build();
    }
}
