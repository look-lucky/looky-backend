package com.looky.domain.admin.dto;

import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.SocialType;
import com.looky.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Role role;
    private SocialType socialType;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .socialType(user.getSocialType())
                .build();
    }
}
