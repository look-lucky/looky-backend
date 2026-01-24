package com.looky.common.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    // 쿠키 만료 시간 (14일)
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 14 * 24 * 60 * 60;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .path("/")              // 전체 경로에서 전송
                .httpOnly(true)         // 자바스크립트 접근 방지 (XSS 방어)
                // 운영 환경 설정
//                .secure(true)
//                .sameSite("None")
                // 로컬 테스트용 설정
                .secure(false)
                .sameSite("Lax")
                .maxAge(REFRESH_TOKEN_EXPIRE_TIME)
                .build();
    }

    public ResponseCookie createExpiredCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(0)              // 즉시 만료
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();
    }
}
