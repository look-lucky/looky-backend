package com.looky.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangeUsernameRequest {
    @NotBlank(message = "새로운 아이디는 필수입니다.")
    private String newUsername;
}
