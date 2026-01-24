package com.looky.domain.admin.dto;

import com.looky.domain.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRoleUpdateRequest {

    @NotNull(message = "권한이 필요합니다.")
    private Role role;
}
