package com.looky.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindPasswordSendCodeRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;
}
