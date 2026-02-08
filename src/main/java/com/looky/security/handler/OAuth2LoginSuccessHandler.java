package com.looky.security.handler;

import com.looky.common.util.CookieUtil;
import com.looky.domain.user.service.RefreshTokenService;
import com.looky.security.details.PrincipalDetails;
import com.looky.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    @Value("${app.redirect-uris.app}")
    private String appRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공");

        // 인증된 유저 정보 가져오기
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        Long userId = principal.getUser().getId();
        String username = principal.getUser().getUsername();
        String role = principal.getUser().getRole().name();

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(userId, username, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, username, role);

        // Refresh Token 저장
        refreshTokenService.save(userId, refreshToken);

        // Refresh Token 쿠키 설정
        response.addHeader("Set-Cookie", cookieUtil.createRefreshTokenCookie(refreshToken).toString());

        // 무조건 앱으로 리다이렉트 (Web 소셜 로그인 미지원)
        String targetUrl = appRedirectUri;

        // 최종 URL 조립. 프론트엔드(앱)으로 리다이렉트 (Access Token은 쿼리 파라미터로 전달)
        String finalUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, finalUrl);
    }
}