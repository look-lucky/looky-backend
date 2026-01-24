package com.looky.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
/*
* 컨트롤러 <-> 서비스 용 DTO
* 클라이언트에 넘겨줄 땐 refreshToken 헤더에 넣고 LoginResponse로
* */
public class AuthTokens {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}