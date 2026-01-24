package com.looky.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private Long expiresIn;

    public static LoginResponse of(String accessToken, long expiresIn) {
        return new LoginResponse(accessToken, expiresIn);
    }
}
