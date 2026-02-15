package com.looky.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SendEmailCodeRequest {
    @NotBlank
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    private Long universityId;
}
