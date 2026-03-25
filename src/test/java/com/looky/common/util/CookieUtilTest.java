package com.looky.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CookieUtil 단위 테스트")
class CookieUtilTest {

    private CookieUtil cookieUtil;

    @BeforeEach
    void setUp() {
        cookieUtil = new CookieUtil();
    }

    // ────────────────────────────────────────────────────────────
    // createRefreshTokenCookie
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createRefreshTokenCookie")
    class CreateRefreshTokenCookie {

        @Test
        @DisplayName("쿠키 이름이 refreshToken 이어야 한다")
        void 쿠키이름이_refreshToken() {
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie("test-token");
            assertThat(cookie.getName()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("전달한 refreshToken 값이 쿠키 값으로 설정된다")
        void 쿠키값이_전달한_토큰과_같다() {
            String token = "eyJhbGciOiJFUzI1NiJ9.sample.token";
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie(token);
            assertThat(cookie.getValue()).isEqualTo(token);
        }

        @Test
        @DisplayName("path 가 / 로 설정되어 전체 경로에서 전송된다")
        void path가_루트() {
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie("token");
            assertThat(cookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("httpOnly 가 true 여야 한다 (XSS 방어)")
        void httpOnly가_true() {
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie("token");
            assertThat(cookie.isHttpOnly()).isTrue();
        }

        @Test
        @DisplayName("만료 시간이 14일(1209600초) 이어야 한다")
        void 만료시간이_14일() {
            long expectedSeconds = 14L * 24 * 60 * 60; // 1_209_600
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie("token");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(expectedSeconds);
        }

        @Test
        @DisplayName("sameSite 가 Lax 로 설정되어 있어야 한다")
        void sameSite가_Lax() {
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie("token");
            assertThat(cookie.getSameSite()).isEqualTo("Lax");
        }

        @Test
        @DisplayName("현재 설정에서 secure 는 false 이어야 한다 (로컬/개발 환경)")
        void secure가_false() {
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie("token");
            assertThat(cookie.isSecure()).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰도 쿠키 값으로 허용된다")
        void 빈문자열_토큰도_허용() {
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie("");
            assertThat(cookie.getValue()).isEmpty();
        }

        @Test
        @DisplayName("긴 JWT 토큰도 잘라내지 않고 그대로 저장된다")
        void 긴_JWT_토큰도_그대로_저장() {
            String longToken = "a".repeat(512);
            ResponseCookie cookie = cookieUtil.createRefreshTokenCookie(longToken);
            assertThat(cookie.getValue()).isEqualTo(longToken);
        }
    }

    // ────────────────────────────────────────────────────────────
    // createExpiredCookie
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createExpiredCookie")
    class CreateExpiredCookie {

        @Test
        @DisplayName("전달한 쿠키 이름이 그대로 사용된다")
        void 전달한_쿠키이름_그대로() {
            ResponseCookie cookie = cookieUtil.createExpiredCookie("refreshToken");
            assertThat(cookie.getName()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("쿠키 값이 빈 문자열이어야 한다")
        void 쿠키값이_빈문자열() {
            ResponseCookie cookie = cookieUtil.createExpiredCookie("refreshToken");
            assertThat(cookie.getValue()).isEmpty();
        }

        @Test
        @DisplayName("maxAge 가 0 이어서 즉시 만료된다")
        void maxAge가_0으로_즉시만료() {
            ResponseCookie cookie = cookieUtil.createExpiredCookie("refreshToken");
            assertThat(cookie.getMaxAge().getSeconds()).isZero();
        }

        @Test
        @DisplayName("path 가 / 로 설정되어 있어야 한다")
        void path가_루트() {
            ResponseCookie cookie = cookieUtil.createExpiredCookie("refreshToken");
            assertThat(cookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("httpOnly 가 true 이어야 한다")
        void httpOnly가_true() {
            ResponseCookie cookie = cookieUtil.createExpiredCookie("refreshToken");
            assertThat(cookie.isHttpOnly()).isTrue();
        }

        @Test
        @DisplayName("sameSite 가 Lax 로 설정되어 있어야 한다")
        void sameSite가_Lax() {
            ResponseCookie cookie = cookieUtil.createExpiredCookie("refreshToken");
            assertThat(cookie.getSameSite()).isEqualTo("Lax");
        }

        @Test
        @DisplayName("임의의 쿠키 이름도 올바르게 처리된다")
        void 임의의_쿠키이름도_처리() {
            ResponseCookie cookie = cookieUtil.createExpiredCookie("accessToken");
            assertThat(cookie.getName()).isEqualTo("accessToken");
            assertThat(cookie.getMaxAge().getSeconds()).isZero();
        }
    }

    // ────────────────────────────────────────────────────────────
    // createRefreshTokenCookie, createExpiredCookie 비교 검증
    // ────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createRefreshTokenCookie vs createExpiredCookie 비교")
    class CompareCookies {

        @Test
        @DisplayName("로그인 시 생성한 쿠키와 로그아웃 시 만료 쿠키의 maxAge 가 다르다")
        void 로그인_로그아웃_maxAge_다름() {
            ResponseCookie loginCookie  = cookieUtil.createRefreshTokenCookie("token");
            ResponseCookie logoutCookie = cookieUtil.createExpiredCookie("refreshToken");

            assertThat(loginCookie.getMaxAge().getSeconds())
                    .isGreaterThan(logoutCookie.getMaxAge().getSeconds());
        }

        @Test
        @DisplayName("두 쿠키 모두 동일한 path 와 httpOnly 설정을 가진다")
        void 두_쿠키_공통_보안_설정() {
            ResponseCookie loginCookie  = cookieUtil.createRefreshTokenCookie("token");
            ResponseCookie logoutCookie = cookieUtil.createExpiredCookie("refreshToken");

            assertThat(loginCookie.getPath()).isEqualTo(logoutCookie.getPath());
            assertThat(loginCookie.isHttpOnly()).isEqualTo(logoutCookie.isHttpOnly());
            assertThat(loginCookie.getSameSite()).isEqualTo(logoutCookie.getSameSite());
        }
    }
}